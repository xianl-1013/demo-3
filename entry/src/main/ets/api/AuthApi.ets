import { ApiClient } from './ApiClient'
import { ApiResult, LoginRequest, LoginResponse } from './model/ApiModels'
import { TokenStorage } from '../utils/TokenStorage'

/**
 * 登录认证接口封装
 */
export class AuthApi {
  /**
   * 登录
   *
   * 对应后端：
   * POST /auth/login
   */
  static async login(username: string, password: string): Promise<ApiResult<LoginResponse>> {
    const bodyObj: LoginRequest = {
      username: username,
      password: password
    }

    const result = await ApiClient.post<LoginResponse>(
      '/auth/login',
      JSON.stringify(bodyObj)
    )

    if (result.code === 200 && result.data && result.data.token) {
      TokenStorage.saveToken(result.data.token)
      console.info('[AuthApi][login] token保存成功')
    }

    return result
  }

  /**
   * 退出登录
   *
   * 第一版前端本地清空 token。
   */
  static logout(): void {
    TokenStorage.clearToken()
  }
}