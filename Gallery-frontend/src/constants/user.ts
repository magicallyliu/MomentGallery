
// y用户文本映射
export const USER_ROLE_MAP: Record<string, string> = {
  user: "普通用户",
  admin: "管理员",
};

// 用户选项映射
export const USER_ROLE_OPTIONS = Object.keys(USER_ROLE_MAP).map((key) => {
  return {
    label: USER_ROLE_MAP[key],
    value: key,
  };
});
