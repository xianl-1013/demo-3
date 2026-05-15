/**
 * TokenStorage
 *
 * 第一版先用内存保存 token。
 * 后面页面跑通后，再升级成本地持久化 preferences。
 */
export class TokenStorage {
  private static token: string = ''

  /**
   * 保存登录 token
   */
  static saveToken(token: string): void {
    TokenStorage.token = token
  }

  /**
   * 获取登录 token
   */
  static getToken(): string {
    return TokenStorage.token
  }

  /**
   * 清空 token
   */
  static clearToken(): void {
    TokenStorage.token = ''
  }

  /**
   * 判断是否已登录
   */
  static hasToken(): boolean {
    return TokenStorage.token.length > 0
  }
}