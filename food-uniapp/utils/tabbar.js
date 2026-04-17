import { normalizeRole, ROLE_ADMIN, ROLE_MERCHANT, ROLE_STUDENT, ROLE_SUPERVISOR } from '@/utils/permission.js';

const icon = (name) => `/static/icon_med/${name}.png`;

const TABBAR_CONFIG = {
  default: [
    { text: '首页', iconPath: icon('shouye'), selectedIconPath: icon('shouye') },
    { text: '投诉', iconPath: icon('tixing'), selectedIconPath: icon('tixing') },
    { text: '消息', iconPath: icon('xiaoxi'), selectedIconPath: icon('xiaoxi') },
    { text: '我的', iconPath: icon('wode'), selectedIconPath: icon('wode') }
  ],
  student: [
    { text: '首页', iconPath: icon('shouye'), selectedIconPath: icon('shouye') },
    { text: '投诉', iconPath: icon('tixing'), selectedIconPath: icon('tixing') },
    { text: '消息', iconPath: icon('xiaoxi'), selectedIconPath: icon('xiaoxi') },
    { text: '我的', iconPath: icon('wode'), selectedIconPath: icon('wode') }
  ],
  merchant: [
    { text: '首页', iconPath: icon('shouye'), selectedIconPath: icon('shouye') },
    { text: '投诉', iconPath: icon('tixing'), selectedIconPath: icon('tixing') },
    { text: '消息', iconPath: icon('xiaoxi'), selectedIconPath: icon('xiaoxi') },
    { text: '店铺', iconPath: icon('shangdian'), selectedIconPath: icon('shangdian') }
  ],
  supervisor: [
    { text: '首页', iconPath: icon('shouye'), selectedIconPath: icon('shouye') },
    { text: '投诉', iconPath: icon('tixing'), selectedIconPath: icon('tixing') },
    { text: '消息', iconPath: icon('xiaoxi'), selectedIconPath: icon('xiaoxi') },
    { text: '管理', iconPath: icon('shezhi'), selectedIconPath: icon('shezhi') }
  ],
  admin: [
    { text: '首页', iconPath: icon('shouye'), selectedIconPath: icon('shouye') },
    { text: '投诉', iconPath: icon('tixing'), selectedIconPath: icon('tixing') },
    { text: '消息', iconPath: icon('xiaoxi'), selectedIconPath: icon('xiaoxi') },
    { text: '管理', iconPath: icon('shezhi'), selectedIconPath: icon('shezhi') }
  ]
};

const resolveTabbarConfig = (userInfo) => {
  const role = normalizeRole(userInfo);
  if (role === ROLE_MERCHANT) return TABBAR_CONFIG.merchant;
  if (role === ROLE_SUPERVISOR) return TABBAR_CONFIG.supervisor;
  if (role === ROLE_ADMIN) return TABBAR_CONFIG.admin;
  if (role === ROLE_STUDENT) return TABBAR_CONFIG.student;
  return TABBAR_CONFIG.default;
};

const applyTabbarConfig = (userInfo) => {
  const config = resolveTabbarConfig(userInfo);
  config.forEach((item, index) => {
    uni.setTabBarItem({
      index,
      text: item.text,
      iconPath: item.iconPath,
      selectedIconPath: item.selectedIconPath
    });
  });
};

export { applyTabbarConfig, resolveTabbarConfig };
