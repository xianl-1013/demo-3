import promptAction from '@ohos.promptAction'
import { HMRouter, HMRouterMgr } from '@hadss/hmrouter'
import { AuthApi } from '../api/AuthApi'

class LoginToHomeParam {
  fromLogin: boolean = true
}

@HMRouter({
  pageUrl: 'LoginPage'
})
@Component
export struct LoginPage {
  @State username: string = 'admin'
  @State password: string = '123456'
  @State statusText: string = '欢迎登录智云会议'
  @State isLoggingIn: boolean = false
  @State showPassword: boolean = false

  // 动态背景圆形开�?
  @State circleAni: boolean = false

  aboutToAppear(): void {
    setTimeout(() => {
      this.circleAni = true
