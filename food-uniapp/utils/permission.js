const ROLE_STUDENT = 'student';
const ROLE_MERCHANT = 'merchant';
const ROLE_SUPERVISOR = 'supervisor';
const ROLE_ADMIN = 'admin';
const ROLE_UNKNOWN = 'unknown';

const normalizeRole = (input) => {
  const role = typeof input === 'string' ? input : input?.userRole;
  if (!role) return ROLE_UNKNOWN;
  const normalized = String(role).trim().toLowerCase();
  if (['student', 'user'].includes(normalized)) return ROLE_STUDENT;
  if (normalized === ROLE_MERCHANT) return ROLE_MERCHANT;
  if (normalized === ROLE_SUPERVISOR) return ROLE_SUPERVISOR;
  if (normalized === ROLE_ADMIN) return ROLE_ADMIN;
  return ROLE_UNKNOWN;
};

const isStudentRole = (input) => normalizeRole(input) === ROLE_STUDENT;
const isMerchantRole = (input) => normalizeRole(input) === ROLE_MERCHANT;
const isSupervisorRole = (input) => normalizeRole(input) === ROLE_SUPERVISOR;
const isAdminRole = (input) => normalizeRole(input) === ROLE_ADMIN;

const roleLabel = (input) => {
  const role = normalizeRole(input);
  if (role === ROLE_STUDENT) return '学生';
  if (role === ROLE_MERCHANT) return '商户';
  if (role === ROLE_SUPERVISOR) return '监督管理员';
  if (role === ROLE_ADMIN) return '管理员';
  return '访客';
};

export {
  ROLE_STUDENT,
  ROLE_MERCHANT,
  ROLE_SUPERVISOR,
  ROLE_ADMIN,
  ROLE_UNKNOWN,
  normalizeRole,
  isStudentRole,
  isMerchantRole,
  isSupervisorRole,
  isAdminRole,
  roleLabel
};
