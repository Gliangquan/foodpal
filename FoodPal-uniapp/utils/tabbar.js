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

// TabBar 页面路径集合，用于判断当前页面是否为 TabBar 页面
const TABBAR_PAGES = [
  'pages/index/index',
  'pages/order/index',
  'pages/notification/index',
  'pages/profile/index'
];

const isTabBarPage = () => {
  const pages = getCurrentPages();
  if (!pages || pages.length === 0) return false;
  const currentPage = pages[pages.length - 1];
  const route = currentPage?.route || '';
  return TABBAR_PAGES.includes(route);
};

const applyTabbarConfig = (userInfo) => {
  // 微信小程序要求 setTabBarItem 必须在 TabBar 页面上调用
  if (!isTabBarPage()) return;
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
