import { Injectable } from '@angular/core';

export type TokenType = 'date' | 'time' | 'tag' | 'ctx' | 'prio' | 'est' | 'recur' | 'project';

export interface NlToken {
  type: TokenType;
  text: string;
  start: number;
  end: number;
}

export interface NlParsed {
  title: string;
  /** ISO date+time string or undefined */
  dueAt?: string;
  /** YYYY-MM-DD only (no time component) */
  dueDate?: string;
  /** ISO local date-time when a clock time was supplied */
  dueDateTime?: string;
  hasTime: boolean;
  projectName?: string;
  tags: string[];
  context?: string;
  /** Internal priority 1..4 (1 = lowest, 4 = highest) */
  priority?: 1 | 2 | 3 | 4;
  estimateMinutes?: number;
  recurrence?: string;
  tokens: NlToken[];
}

const DAY_TOKENS: Record<string, number> = {
  lundi: 1, mardi: 2, mercredi: 3, jeudi: 4, vendredi: 5, samedi: 6, dimanche: 0,
  lun: 1, mar: 2, mer: 3, jeu: 4, ven: 5, sam: 6, dim: 0,
};

function startOfDay(d: Date): Date {
  const x = new Date(d);
  x.setHours(0, 0, 0, 0);
  return x;
}

function nextWeekday(target: number, from: Date): Date {
  const f = startOfDay(from);
  const cur = f.getDay();
  let add = (target - cur + 7) % 7;
  if (add === 0) add = 7;
  const d = new Date(f);
  d.setDate(d.getDate() + add);
  return d;
}

function pad2(n: number): string {
  return n.toString().padStart(2, '0');
}

function localIso(d: Date): string {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}T${pad2(d.getHours())}:${pad2(d.getMinutes())}:00`;
}

function dateOnlyIso(d: Date): string {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`;
}

@Injectable({ providedIn: 'root' })
export class NlParserService {
  parse(input: string, anchor?: Date): NlParsed {
    const text = input || '';
    const today = anchor ? new Date(anchor) : new Date();
    const tokens: NlToken[] = [];

    let dueAt: string | undefined;
    let dueDate: string | undefined;
    let dueDateTime: string | undefined;
    let hasTime = false;
    const tags: string[] = [];
    let context: string | undefined;
    let priority: 1 | 2 | 3 | 4 | undefined;
    let estimateMinutes: number | undefined;
    let recurrence: string | undefined;
    let projectName: string | undefined;

    // Priority — !!!! / !!! / !! / !
    const prioRe = /(^|\s)(!{1,4})(?=\s|$)/g;
    let m: RegExpExecArray | null;
    while ((m = prioRe.exec(text)) !== null) {
      const marks = m[2];
      const display: 1 | 2 | 3 | 4 = marks.length >= 3 ? 1 : marks.length === 2 ? 2 : 3;
      // Convert "design priority" to internal priority (1 lowest, 4 highest).
      priority = (5 - display) as 1 | 2 | 3 | 4;
      tokens.push({
        type: 'prio',
        text: marks,
        start: m.index + m[1].length,
        end: m.index + m[1].length + marks.length,
      });
    }

    // #tags
    const tagRe = /#([a-zA-Zà-üÀ-Ü0-9_-]+)/g;
    while ((m = tagRe.exec(text)) !== null) {
      tags.push(m[1].toLowerCase());
      tokens.push({ type: 'tag', text: m[0], start: m.index, end: m.index + m[0].length });
    }

    // #project: nope — same syntax as tag in this app, the project lookup happens elsewhere.
    // We leave projectName undefined here; the QuickAdd component resolves it from labels.

    // @context
    const ctxRe = /@([a-zA-Zà-üÀ-Ü0-9_-]+)/g;
    while ((m = ctxRe.exec(text)) !== null) {
      context = m[1].toLowerCase();
      tokens.push({ type: 'ctx', text: m[0], start: m.index, end: m.index + m[0].length });
    }

    // Estimate ~2h, ~30min, ~1h30
    const estRe = /~(\d+)(h|min|m)(\d{1,2})?/gi;
    while ((m = estRe.exec(text)) !== null) {
      const n = parseInt(m[1], 10);
      const unit = m[2].toLowerCase();
      const sub = m[3] ? parseInt(m[3], 10) : 0;
      estimateMinutes = unit === 'h' ? n * 60 + sub : n;
      tokens.push({ type: 'est', text: m[0], start: m.index, end: m.index + m[0].length });
    }

    // Recurrence
    const recurRe = /(tous les (lundis?|mardis?|mercredis?|jeudis?|vendredis?|samedis?|dimanches?)|chaque (jour|semaine|mois)|quotidien|hebdo)/gi;
    while ((m = recurRe.exec(text)) !== null) {
      const w = m[0];
      if (/jour|quotidien/i.test(w)) recurrence = 'daily';
      else if (/semaine|hebdo|tous les/i.test(w)) recurrence = 'weekly';
      else if (/mois/i.test(w)) recurrence = 'monthly';
      tokens.push({ type: 'recur', text: m[0], start: m.index, end: m.index + m[0].length });
    }

    // Time (14h, 14h30, 9:00)
    let time: { h: number; m: number } | null = null;
    const timeRe = /\b(\d{1,2})(h|:)(\d{2})?\b/g;
    while ((m = timeRe.exec(text)) !== null) {
      const h = parseInt(m[1], 10);
      const mn = m[3] ? parseInt(m[3], 10) : 0;
      if (h >= 0 && h <= 23 && mn >= 0 && mn <= 59) {
        time = { h, m: mn };
        hasTime = true;
        tokens.push({ type: 'time', text: m[0], start: m.index, end: m.index + m[0].length });
      }
    }

    // Date words
    const dateRe = /\b(aujourd'?hui|demain|apres-?demain|après-?demain|hier|ce soir|ce matin|cet apres-?midi|cet après-?midi|lundi|mardi|mercredi|jeudi|vendredi|samedi|dimanche|lun|mar|mer|jeu|ven|sam|dim)\b/gi;
    let baseDate: Date | null = null;
    {
      const localM = dateRe.exec(text);
      if (localM) {
        const w = localM[0].toLowerCase().replace(/[' -]/g, '');
        let d = new Date(today);
        if (w.startsWith('aujourd')) d = new Date(today);
        else if (w === 'demain') d.setDate(d.getDate() + 1);
        else if (w.startsWith('apresdemain') || w.startsWith('après') || w.startsWith('aprèsdemain')) d.setDate(d.getDate() + 2);
        else if (w === 'hier') d.setDate(d.getDate() - 1);
        else if (w === 'cesoir') { d = new Date(today); time = time || { h: 19, m: 0 }; hasTime = true; }
        else if (w === 'cematin') { d = new Date(today); time = time || { h: 9, m: 0 }; hasTime = true; }
        else if (w === 'cetapresmidi' || w === 'cetapresmidi') { d = new Date(today); time = time || { h: 14, m: 0 }; hasTime = true; }
        else if (DAY_TOKENS[w] !== undefined) {
          d = nextWeekday(DAY_TOKENS[w], today);
        }
        baseDate = d;
        tokens.push({ type: 'date', text: localM[0], start: localM.index, end: localM.index + localM[0].length });
      }
    }

    // dd/mm fallback
    if (!baseDate) {
      const dmRe = /\b(\d{1,2})\/(\d{1,2})\b/g;
      const dm = dmRe.exec(text);
      if (dm) {
        const day = parseInt(dm[1], 10), mon = parseInt(dm[2], 10);
        if (day >= 1 && day <= 31 && mon >= 1 && mon <= 12) {
          const d = new Date(today.getFullYear(), mon - 1, day);
          if (d < startOfDay(today)) d.setFullYear(d.getFullYear() + 1);
          baseDate = d;
          tokens.push({ type: 'date', text: dm[0], start: dm.index, end: dm.index + dm[0].length });
        }
      }
    }

    if (baseDate) {
      if (time) {
        baseDate.setHours(time.h, time.m, 0, 0);
        dueDateTime = localIso(baseDate);
        dueAt = dueDateTime;
        dueDate = dateOnlyIso(baseDate);
      } else {
        baseDate.setHours(0, 0, 0, 0);
        dueDate = dateOnlyIso(baseDate);
        dueAt = baseDate.toISOString();
      }
    } else if (time) {
      const d = new Date(today);
      d.setHours(time.h, time.m, 0, 0);
      dueDateTime = localIso(d);
      dueAt = dueDateTime;
      dueDate = dateOnlyIso(d);
      hasTime = true;
    }

    tokens.sort((a, b) => a.start - b.start);

    // Stripped title (used as the persisted task content)
    let title = text;
    const sortedDesc = [...tokens].sort((a, b) => b.start - a.start);
    for (const tk of sortedDesc) {
      title = title.slice(0, tk.start) + title.slice(tk.end);
    }
    title = title.replace(/\s{2,}/g, ' ').trim();

    return {
      title,
      dueAt,
      dueDate,
      dueDateTime,
      hasTime,
      projectName,
      tags,
      context,
      priority,
      estimateMinutes,
      recurrence,
      tokens,
    };
  }

  /**
   * Build segments for visual rendering: each segment has the raw text and (if matched) a token type.
   */
  segments(input: string, parsed: NlParsed): { text: string; type: TokenType | null }[] {
    if (!parsed.tokens.length) return input ? [{ text: input, type: null }] : [];
    const out: { text: string; type: TokenType | null }[] = [];
    let cur = 0;
    for (const tk of parsed.tokens) {
      if (tk.start > cur) out.push({ text: input.slice(cur, tk.start), type: null });
      out.push({ text: input.slice(tk.start, tk.end), type: tk.type });
      cur = tk.end;
    }
    if (cur < input.length) out.push({ text: input.slice(cur), type: null });
    return out;
  }
}
