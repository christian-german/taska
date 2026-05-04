export type ViewStyle = 'LIST' | 'BOARD' | 'CALENDAR';

export interface TimeEntry {
  id: string;
  startAt: string;    // "2024-05-03T10:00:00" (LocalDateTime, no tz)
  endAt: string;      // "2024-05-03T11:30:00"
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

export interface Task {
  id: string;
  content: string;
  description?: string;
  projectId?: string;
  sectionId?: string;
  parentId?: string;
  order: number;
  priority: 1 | 2 | 3 | 4;
  labels: string[];
  isCompleted: boolean;
  dueDate?: string;
  dueDateTime?: string;
  isRecurring: boolean;
  estimateMinutes?: number;
  mentionContext?: string;
  recurrenceRule?: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
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
 * Internal priority semantics: 1 = lowest (normal), 4 = highest (urgent).
 * Design / Todoist semantics: P1 = highest, P4 = lowest.
 * Map back and forth for display.
 */
export function displayPriority(internal: number): 1 | 2 | 3 | 4 {
  return (5 - Math.max(1, Math.min(4, internal))) as 1 | 2 | 3 | 4;
}

export function internalPriority(display: number): 1 | 2 | 3 | 4 {
  return (5 - Math.max(1, Math.min(4, display))) as 1 | 2 | 3 | 4;
}

export const DESIGN_PRIORITY_COLOR: Record<number, string> = {
  1: 'var(--p1)',
  2: 'var(--p2)',
  3: 'var(--p3)',
  4: 'var(--p4)',
};

export const PRIORITY_LABELS: Record<number, string> = {
  1: 'Normale',
  2: 'Moyenne',
  3: 'Haute',
  4: 'Urgente',
};

export function getColor(colorName: string): string {
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

export function isOverdue(taskOrDate: Pick<Task, 'dueDate' | 'isCompleted'> | string | undefined): boolean {
  if (!taskOrDate) return false;
  if (typeof taskOrDate === 'string') {
    return taskOrDate < new Date().toISOString().split('T')[0];
  }
  if (!taskOrDate.dueDate || taskOrDate.isCompleted) return false;
  return taskOrDate.dueDate < new Date().toISOString().split('T')[0];
}

export function formatDueDate(dueDate?: string): string {
  if (!dueDate) return '';
  const date = new Date(dueDate + 'T00:00:00');
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const tomorrow = new Date(today);
  tomorrow.setDate(today.getDate() + 1);
  if (date.getTime() === today.getTime()) return "Aujourd'hui";
  if (date.getTime() === tomorrow.getTime()) return 'Demain';
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

export function isToday(dueDate?: string): boolean {
  if (!dueDate) return false;
  return dueDate === new Date().toISOString().split('T')[0];
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

export function getTaskDueDateTime(task: Pick<Task, 'dueDate' | 'dueDateTime'>): Date | null {
  if (task.dueDateTime) return new Date(task.dueDateTime);
  if (task.dueDate) return new Date(task.dueDate + 'T00:00:00');
  return null;
}

export function taskHasTime(task: Pick<Task, 'dueDateTime'>): boolean {
  return !!task.dueDateTime;
}
