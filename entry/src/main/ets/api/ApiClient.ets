import http from '@ohos.net.http'
import { TokenStorage } from '../utils/TokenStorage'
import { ApiResult } from './model/ApiModels'

/**
 * HTTP 请求客户端
 *
 * 作用：
 * 1. 统一 baseUrl
 * 2. 统一 Authorization token
 * 3. 统一 JSON 请求
 * 4. 统一解析后端 Result
 */
export class ApiClient {
  /**
   * 后端基础地址
   *
   * 注意：
   * 真机不能用 localhost。
   * 这里要改成你电脑的局域网 IP。
   */
  private static BASE_URL: string = 'http://192.168.1.26:8080'

  /**
   * 修改后端基础地址
   */
  static setBaseUrl(baseUrl: string): void {
    ApiClient.BASE_URL = baseUrl
  }

  /**
   * 获取后端基础地址
   */
  static getBaseUrl(): string {
    return ApiClient.BASE_URL
  }

  /**
   * GET 请求
   */
  static async get<T>(path: string): Promise<ApiResult<T>> {
    return ApiClient.request<T>('GET', path, '')
  }

  /**
   * POST 请求
   */
  static async post<T>(path: string, body: string): Promise<ApiResult<T>> {
    return ApiClient.request<T>('POST', path, body)
  }

  /**
   * 统一请求方法
   */
  private static async request<T>(
    method: string,
    path: string,
    body: string
  ): Promise<ApiResult<T>> {
    const request = http.createHttp()
    const url = ApiClient.BASE_URL + path

    const token: string = TokenStorage.getToken()

    let header: Record<string, string> = {
      'Content-Type': 'application/json'
    }

    if (token.length > 0) {
      header['Authorization'] = 'Bearer ' + token
    }

    try {
      console.info('[ApiClient][request] url=' + url)
      console.info('[ApiClient][request] method=' + method)
      console.info('[ApiClient][request] body=' + body)

      const response = await request.request(url, {
        method: ApiClient.toRequestMethod(method),
        header: header,
        extraData: body,
        connectTimeout: 10000,
        readTimeout: 10000
      })

      const responseText: string = String(response.result)

      console.info('[ApiClient][response] ' + responseText)

      const result = JSON.parse(responseText) as ApiResult<T>

      return result
    } catch (error) {
      console.error('[ApiClient][error] ' + JSON.stringify(error))

      const failResult: ApiResult<T> = {
        code: 500,
        msg: '网络请求失败',
        data: null as T
      }

      return failResult
    } finally {
      request.destroy()
    }
  }

  /**
   * 字符串方法转换成鸿蒙 http.RequestMethod
   */
  private static toRequestMethod(method: string): http.RequestMethod {
    if (method === 'POST') {
      return http.RequestMethod.POST
    }

    if (method === 'PUT') {
      return http.RequestMethod.PUT
    }

    if (method === 'DELETE') {
      return http.RequestMethod.DELETE
    }

    return http.RequestMethod.GET
  }
}