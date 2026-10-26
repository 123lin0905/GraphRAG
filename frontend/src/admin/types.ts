import type { Option } from '@/utils/dicts'

export type FieldType = 'text' | 'textarea' | 'number' | 'select' | 'switch' | 'date' | 'datetime' | 'image' | 'rate' | 'password' | 'json'
export type TagType = 'success' | 'warning' | 'info' | 'danger' | 'primary'

export interface CrudField {
  prop: string
  label: string
  type?: FieldType
  options?: Option[]
  dict?: string
  table?: boolean
  form?: boolean
  width?: number
  minWidth?: number
  required?: boolean
  filter?: boolean
  span?: 1 | 2
  placeholder?: string
  min?: number
  max?: number
  step?: number
  precision?: number
  tags?: Record<string | number, TagType>
  formatter?: (row: any) => string
  default?: any
  ellipsis?: boolean
}
export interface CrudCtx { reload: () => void }
export interface RowAction {
  label: string
  type?: TagType
  icon?: string
  show?: (row: any) => boolean
  confirm?: string | ((row: any) => string)
  handler: (row: any, ctx: CrudCtx) => unknown
}
export interface ToolbarAction {
  label: string
  type?: TagType
  icon?: string
  handler: (ctx: CrudCtx) => unknown
}
export interface CrudConfig {
  key: string
  title: string
  group: string
  icon: string
  desc?: string
  api: string
  pageUrl?: string
  roles?: string[]
  create?: boolean
  edit?: boolean
  remove?: boolean
  keyword?: boolean
  keywordPlaceholder?: string
  dialogWidth?: string
  fields: CrudField[]
  rowActions?: RowAction[]
  toolbarActions?: ToolbarAction[]
}
