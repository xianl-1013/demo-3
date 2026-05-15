/**
 * 后端统一返回结构
 *
 * 后端所有接口都返回：
 * {
 *   code: 200,
 *   msg: "操作成功",
 *   data: {}
 * }
 */
export interface ApiResult<T> {
  code: number
  msg: string
  data: T
}

/**
 * 系统配置返回数据
 *
 * 对应后端：
 * GET /system/config
 */
export interface SystemConfigResponse {
  httpBaseUrl: string
  meetingWsUrl: string
}

/**
 * 登录响应数据
 *
 * 对应后端：
 * POST /auth/login
 */
export interface LoginResponse {
  token: string
  userId: string
  userName: string
  avatar: string
  phone: string
}

/**
 * 登录请求参数
 */
export interface LoginRequest {
  username: string
  password: string
}