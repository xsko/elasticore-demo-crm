import type { ThemeConfig } from 'antd';

/**
 * A working tool's look: a dark navigation rail, a quiet canvas, white panels with hairline
 * borders and one deep teal for actions. Numbers line up (tabular figures) so columns of
 * amounts read at a glance. The same values are CSS variables in styles.css.
 */
export const colors = {
  rail: '#101b28',
  canvas: '#f4f5f7',
  panel: '#ffffff',
  line: '#e2e5ea',
  lineSoft: '#eef0f3',
  text: '#111a25',
  textSoft: '#4a5466',
  textFaint: '#8a93a2',
  primary: '#0d6b78',
  primaryHover: '#0a5862',
  primarySoft: '#e5f1f2',
  success: '#2b7a4b',
  warning: '#a8670d',
  danger: '#b23a2e',
};

export const theme: ThemeConfig = {
  token: {
    colorPrimary: colors.primary,
    colorPrimaryHover: colors.primaryHover,
    colorInfo: colors.primary,
    colorLink: colors.primary,
    colorSuccess: colors.success,
    colorWarning: colors.warning,
    colorError: colors.danger,
    colorText: colors.text,
    colorTextSecondary: colors.textSoft,
    colorTextTertiary: colors.textFaint,
    colorBorder: '#d5dae1',
    colorBorderSecondary: colors.lineSoft,
    colorBgLayout: colors.canvas,
    borderRadius: 6,
    borderRadiusLG: 8,
    borderRadiusSM: 4,
    fontSize: 13,
    controlHeight: 32,
    fontFamily: '"Pretendard Variable", Pretendard, -apple-system, "Segoe UI", "Malgun Gothic", system-ui, sans-serif',
    boxShadowSecondary: '0 4px 16px rgba(16, 27, 40, 0.10), 0 1px 3px rgba(16, 27, 40, 0.06)',
    motionDurationMid: '0.12s',
  },
  components: {
    Menu: {
      darkItemBg: colors.rail, darkSubMenuItemBg: colors.rail, darkItemColor: '#a9b3c1', darkItemHoverColor: '#ffffff',
      darkItemHoverBg: 'rgba(255,255,255,0.06)', darkItemSelectedBg: 'rgba(255,255,255,0.10)', darkItemSelectedColor: '#ffffff',
      darkGroupTitleColor: '#6d788a', itemHeight: 34, itemBorderRadius: 6, itemMarginInline: 10, groupTitleFontSize: 11, iconMarginInlineEnd: 11,
    },
    Table: {
      headerBg: '#fafbfc', headerColor: colors.textSoft, headerSplitColor: 'transparent', rowHoverBg: '#f6f9fa',
      rowSelectedBg: '#edf6f7', rowSelectedHoverBg: '#e5f1f2', cellPaddingBlock: 11, cellPaddingInline: 12,
      cellPaddingBlockSM: 7, cellPaddingInlineSM: 12, borderColor: colors.lineSoft, headerBorderRadius: 0,
    },
    Card: { headerFontSize: 14, paddingLG: 18 },
    Tabs: { horizontalItemPadding: '12px 2px', titleFontSize: 13, inkBarColor: colors.primary, itemSelectedColor: colors.text, itemColor: colors.textSoft },
    Button: { fontWeight: 500, primaryShadow: 'none', defaultShadow: 'none', dangerShadow: 'none', paddingInline: 14 },
    Segmented: { itemSelectedBg: '#ffffff', trackBg: '#eceef2' },
    Tag: { defaultBg: '#f1f3f6' },
    Drawer: { paddingLG: 24 },
    Input: { activeShadow: '0 0 0 3px rgba(13, 107, 120, 0.12)' },
    Select: { optionSelectedBg: colors.primarySoft },
    Pagination: { itemActiveBg: colors.primarySoft },
  },
};
