import {
  BookOpen, Building2, CalendarCheck, Circle, Contact, FileText, LifeBuoy, Package, TrendingUp, UserPlus, Users,
  type LucideProps,
} from 'lucide-react';
import type { ComponentType } from 'react';

/** The icons an entity line may name with @ui(icon=...). An unknown name gets a plain mark. */
const ICONS: Record<string, ComponentType<LucideProps>> = {
  'book-open': BookOpen,
  'building-2': Building2,
  'calendar-check': CalendarCheck,
  contact: Contact,
  'file-text': FileText,
  'life-buoy': LifeBuoy,
  package: Package,
  'trending-up': TrendingUp,
  'user-plus': UserPlus,
  users: Users,
};

export function EntityIcon({ name, size = 16, className }: { name: string; size?: number; className?: string }) {
  const Icon = ICONS[name] ?? Circle;
  return <Icon size={size} strokeWidth={1.75} className={className} />;
}
