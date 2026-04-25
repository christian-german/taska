export type ViewStyle = 'LIST' | 'BOARD' | 'CALENDAR';

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

export const PRIORITY_LABELS: Record<number, string> = {
  1: 'Normal',
  2: 'Medium',
  3: 'High',
  4: 'Urgent',
};

export function getColor(colorName: string): string {
  return PROJECT_COLORS[colorName] ?? PROJECT_COLORS['charcoal'];
}

export function isOverdue(dueDate?: string): boolean {
  if (!dueDate) return false;
  return new Date(dueDate) < new Date(new Date().toDateString());
}

export function isToday(dueDate?: string): boolean {
  if (!dueDate) return false;
  return dueDate === new Date().toISOString().split('T')[0];
}

export function formatDueDate(dueDate?: string): string {
  if (!dueDate) return '';
  const date = new Date(dueDate + 'T00:00:00');
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const tomorrow = new Date(today);
  tomorrow.setDate(today.getDate() + 1);
  if (date.getTime() === today.getTime()) return 'Today';
  if (date.getTime() === tomorrow.getTime()) return 'Tomorrow';
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}
