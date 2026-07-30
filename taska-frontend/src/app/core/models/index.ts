export type ViewStyle = 'LIST' | 'BOARD' | 'CALENDAR';

export interface TimeEntry {
  id: string;
  startAt: string;    // ISO 8601 UTC, e.g. "2024-05-03T10:00:00Z"
  endAt: string;      // ISO 8601 UTC, e.g. "2024-05-03T11:30:00Z"
  projectId: string;
  description: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export function timeEntryDuration(e: TimeEntry): number {
  return Math.round((new Date(e.endAt).getTime() - new Date(e.startAt).getTime()) / 60000);
}

export interface Filter {
  id: string;
  name: string;
  color: string;
  isFavorite: boolean;
  order: number;
  projectId?: string;
  hasDate?: boolean;
}

export interface Project {
  id: string;
  name: string;
  color: string;
  parentId?: string;
  order: number;
  isFavorite: boolean;
  viewStyle: ViewStyle;
  isInboxProject: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Section {
  id: string;
  name: string;
  projectId: string;
  order: number;
  createdAt: string;
}

export type RecurrenceScope = 'THIS_ONLY' | 'FROM_THIS';
export type TaskType = 'TODO' | 'MEETING';

export interface Task {
  id: string;
  content: string;
  /** Missing only for responses from servers predating task types. */
  type?: TaskType;
  description?: string;
  projectId?: string;
  sectionId?: string;
  parentId?: string;
  order: number;
  priority: 1 | 2 | 3 | 4;
  labels: string[];
  isCompleted: boolean;
  dueAt: string | null;
  allDay: boolean;
  isRecurring: boolean;
  estimateMinutes?: number;
  mentionContext?: string;
  recurrenceRule?: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  instanceId?: string | null;
  scheduledAt?: string | null;
  isVirtual?: boolean;
  rruleEndsAt?: string | null;
}

export interface Label {
  id: string;
  name: string;
  color: string;
  order: number;
  isFavorite: boolean;
}

export interface Comment {
  id: string;
  taskId?: string;
  projectId?: string;
  content: string;
  createdAt: string;
}

export interface DailyCount {
  date: string;
  count: number;
}

export interface ProjectStat {
  projectId: string;
  name: string;
  color: string;
  total: number;
  done: number;
}

export interface StatsOverview {
  totalCompleted: number;
  totalActive: number;
  overdue: number;
  streakDays: number;
  completedThisWeek: number;
  remainingMinutes: number;
  last14Days: DailyCount[];
  byProject: ProjectStat[];
}

export const PROJECT_COLORS: Record<string, string> = {
  berry_red: '#b8256f',
  red: '#db4035',
  orange: '#ff9933',
  yellow: '#fad000',
  olive_green: '#afb83b',
  lime_green: '#7ecc49',
  green: '#299438',
  mint_green: '#6accbc',
  teal: '#158fad',
  sky_blue: '#14aaf5',
  light_blue: '#96c3eb',
  blue: '#4073ff',
  grape: '#884dff',
  violet: '#af38eb',
  lavender: '#eb96eb',
  magenta: '#e05194',
  salmon: '#ff8d85',
  charcoal: '#808080',
  grey: '#b8b8b8',
  taupe: '#ccac93',
};

/**
 * Internal priority semantics: 1 = highest, 4 = lowest.
 */
export function displayPriority(internal: number): 1 | 2 | 3 | 4 {
  return (5 - Math.max(1, Math.min(4, internal))) as 1 | 2 | 3 | 4;
}

export const PRIORITY_LABELS: Record<number, string> = {
  1: 'Urgente',
  2: 'Haute',
  3: 'Moyenne',
  4: 'Normale',
};

export function getColor(colorName: string): string {
  if (colorName.startsWith('#')) return colorName;
  return PROJECT_COLORS[colorName] ?? PROJECT_COLORS['charcoal'];
}

export function hexToRgba(hex: string | undefined, a: number): string {
  if (!hex) return `rgba(138,132,122,${a})`;
  if (hex.startsWith('rgba') || hex.startsWith('rgb')) return hex;
  let h = hex.replace('#', '');
  if (h.length === 3) h = h.split('').map(c => c + c).join('');
  const r = parseInt(h.slice(0, 2), 16);
  const g = parseInt(h.slice(2, 4), 16);
  const b = parseInt(h.slice(4, 6), 16);
  return `rgba(${r},${g},${b},${a})`;
}

export function isOverdue(taskOrDate: Pick<Task, 'dueAt' | 'allDay' | 'isCompleted'> | string | undefined): boolean {
  if (!taskOrDate) return false;
  const todayMidnight = new Date();
  todayMidnight.setHours(0, 0, 0, 0);
  if (typeof taskOrDate === 'string') {
    const d = new Date(taskOrDate);
    d.setHours(0, 0, 0, 0);
    return d < todayMidnight;
  }
  if (!taskOrDate.dueAt || taskOrDate.isCompleted) return false;
  const d = new Date(taskOrDate.dueAt);
  d.setHours(0, 0, 0, 0);
  return d < todayMidnight;
}

export function formatDueDate(dueAt?: string | null): string {
  if (!dueAt) return '';
  const date = new Date(dueAt);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const tomorrow = new Date(today);
  tomorrow.setDate(today.getDate() + 1);
  const d = new Date(date);
  d.setHours(0, 0, 0, 0);
  if (d.getTime() === today.getTime()) return "Aujourd'hui";
  if (d.getTime() === tomorrow.getTime()) return 'Demain';
  return fmtDateShort(date);
}

export const PRIORITY_BORDER_COLORS: Record<number, string> = {
  1: 'border-gray-400',
  2: 'border-blue-500',
  3: 'border-orange-500',
  4: 'border-red-500',
};

export const PRIORITY_TEXT_COLORS: Record<number, string> = {
  1: 'text-gray-400',
  2: 'text-blue-500',
  3: 'text-orange-500',
  4: 'text-red-500',
};

export function isToday(dueAt?: string | null): boolean {
  if (!dueAt) return false;
  const d = new Date(dueAt);
  const t = new Date();
  return d.getFullYear() === t.getFullYear() && d.getMonth() === t.getMonth() && d.getDate() === t.getDate();
}

export function startOfDay(d: Date | string): Date {
  const x = typeof d === 'string' ? new Date(d) : new Date(d);
  x.setHours(0, 0, 0, 0);
  return x;
}

export function sameDay(a: Date | string, b: Date | string): boolean {
  return startOfDay(a).getTime() === startOfDay(b).getTime();
}

export function daysDiff(a: Date | string, b: Date | string): number {
  return Math.round((startOfDay(b).getTime() - startOfDay(a).getTime()) / 86400000);
}

const FR_DAYS_LONG = ['dimanche', 'lundi', 'mardi', 'mercredi', 'jeudi', 'vendredi', 'samedi'];
const FR_MONTHS_LONG = ['janvier', 'février', 'mars', 'avril', 'mai', 'juin', 'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre'];
const FR_MONTHS_SHORT = ['janv.', 'févr.', 'mars', 'avr.', 'mai', 'juin', 'juil.', 'août', 'sept.', 'oct.', 'nov.', 'déc.'];
const FR_DAYS_SHORT = ['dim.', 'lun.', 'mar.', 'mer.', 'jeu.', 'ven.', 'sam.'];

export function fmtDateLong(d: Date | string): string {
  const x = typeof d === 'string' ? new Date(d) : d;
  return `${FR_DAYS_LONG[x.getDay()]} ${x.getDate()} ${FR_MONTHS_LONG[x.getMonth()]}`;
}

export function fmtDateShort(d: Date | string): string {
  const x = typeof d === 'string' ? new Date(d) : d;
  return `${x.getDate()} ${FR_MONTHS_SHORT[x.getMonth()]}`;
}

export function fmtTime(d: Date | string): string {
  const x = typeof d === 'string' ? new Date(d) : d;
  return x.getHours().toString().padStart(2, '0') + ':' + x.getMinutes().toString().padStart(2, '0');
}

export function fmtRel(due: Date | string | undefined): string {
  if (!due) return '';
  const today = new Date();
  const target = typeof due === 'string' ? new Date(due) : due;
  const diff = daysDiff(today, target);
  if (diff === 0) return 'auj.';
  if (diff === -1) return 'hier';
  if (diff === 1) return 'demain';
  if (diff > 1 && diff < 7) return FR_DAYS_SHORT[target.getDay()];
  if (diff < 0) return Math.abs(diff) + 'j en retard';
  return fmtDateShort(target);
}

export function fmtEstimate(min?: number | null): string {
  if (!min) return '';
  const h = Math.floor(min / 60);
  const m = min % 60;
  return h ? (m ? `${h}h${m.toString().padStart(2, '0')}` : `${h}h`) : `${m}min`;
}

export function getTaskDueDate(task: Pick<Task, 'dueAt'>): Date | null {
  return task.dueAt ? new Date(task.dueAt) : null;
}

export function isTaskAllDay(task: Pick<Task, 'allDay'>): boolean {
  return task.allDay;
}
