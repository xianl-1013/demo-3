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

  // 动态背景圆形开关
  @State circleAni: boolean = false

  aboutToAppear(): void {
    setTimeout(() => {
      this.circleAni = true
    }, 120)
  }

  build() {
    Stack() {
      // 页面基础背景
      Column() {
      }
      .width('100%')
      .height('100%')
      .backgroundColor('#F5F8FF')

      // 左上动态柔光圆
      Column() {
      }
      .width(230)
      .height(230)
      .borderRadius(120)
      .backgroundColor('#DDE8FF')
      .opacity(this.circleAni ? 0.72 : 0.95)
      .position({ x: -95, y: 70 })
      .translate({
        x: this.circleAni ? 18 : 0,
        y: this.circleAni ? -22 : 0
      })
      .scale({
        x: this.circleAni ? 1.08 : 1,
        y: this.circleAni ? 1.08 : 1
      })
      .animation({
        duration: 1800,
        curve: Curve.EaseInOut,
        iterations: -1,
        playMode: PlayMode.Alternate
      })

      // 右上动态柔光圆
      Column() {
      }
      .width(150)
      .height(150)
      .borderRadius(80)
      .backgroundColor('#D6F0FF')
      .opacity(this.circleAni ? 0.95 : 0.68)
      .position({ x: '72%', y: 56 })
      .translate({
        x: this.circleAni ? -20 : 0,
        y: this.circleAni ? 24 : 0
      })
      .scale({
        x: this.circleAni ? 0.92 : 1,
        y: this.circleAni ? 0.92 : 1
      })
      .animation({
        duration: 2200,
        curve: Curve.EaseInOut,
        iterations: -1,
        playMode: PlayMode.Alternate
      })

      // 底部小动态圆
      Column() {
      }
      .width(90)
      .height(90)
      .borderRadius(50)
      .backgroundColor('#EEF4FF')
      .opacity(this.circleAni ? 0.55 : 0.9)
      .position({ x: '6%', y: '76%' })
      .translate({
        x: this.circleAni ? 28 : 0,
        y: this.circleAni ? 16 : 0
      })
      .scale({
        x: this.circleAni ? 1.15 : 1,
        y: this.circleAni ? 1.15 : 1
      })
      .animation({
        duration: 2000,
        curve: Curve.EaseInOut,
        iterations: -1,
        playMode: PlayMode.Alternate
      })

      // 顶部品牌区
      Column() {
        Row() {
          Column() {
            Text('智云会议')
              .fontSize(34)
              .fontWeight(FontWeight.Bold)
              .fontColor('#1E293B')

            Text('安全高效的音视频协作平台')
              .fontSize(15)
              .fontColor('#6B7280')
              .margin({ top: 8 })
          }
          .layoutWeight(1)
          .alignItems(HorizontalAlign.Start)

          Column() {
            Text('云')
              .fontSize(22)
              .fontWeight(FontWeight.Bold)
              .fontColor('#FFFFFF')
          }
          .width(54)
          .height(54)
          .borderRadius(18)
          .backgroundColor('#2F68FF')
          .justifyContent(FlexAlign.Center)
          .alignItems(HorizontalAlign.Center)
          .shadow({
            radius: 16,
            color: '#332F68FF',
            offsetX: 0,
            offsetY: 8
          })
        }
        .width('100%')

        Row() {
          this.buildBrandTag('高清视频')
          this.buildBrandTag('实时语音')
          this.buildBrandTag('安全会议')
        }
        .width('100%')
        .justifyContent(FlexAlign.Start)
        .margin({ top: 22 })
      }
      .width('100%')
      .padding({ left: 28, right: 28, top: 88 })

      // 登录卡片
      Column() {
        Column() {
          Row() {
            Column() {
              Text('账号登录')
                .fontSize(26)
                .fontWeight(FontWeight.Bold)
                .fontColor('#1F2937')

              Text(this.statusText)
                .fontSize(13)
                .fontColor('#8A93A3')
                .margin({ top: 5 })
            }
            .alignItems(HorizontalAlign.Start)
            .layoutWeight(1)

            Text('企业版')
              .fontSize(12)
              .fontWeight(FontWeight.Bold)
              .fontColor('#2F68FF')
              .padding({ left: 10, right: 10, top: 5, bottom: 5 })
              .backgroundColor('#EAF1FF')
              .borderRadius(14)
          }
          .width('100%')
          .margin({ bottom: 22 })

          this.buildInputLabel('账号')

          Row() {
            TextInput({ placeholder: '请输入账号', text: this.username })
              .layoutWeight(1)
              .height(46)
              .fontSize(15)
              .fontColor('#222222')
              .placeholderColor('#A0A7B5')
              .backgroundColor(Color.Transparent)
              .padding({ left: 0, right: 0 })
              .onChange((value: string) => {
                this.username = value
              })
          }
          .width('100%')
          .height(48)
          .padding({ left: 14, right: 14 })
          .borderRadius(14)
          .backgroundColor('#F6F8FC')
          .margin({ bottom: 14 })

          this.buildInputLabel('密码')

          Row() {
            TextInput({ placeholder: '请输入密码', text: this.password })
              .layoutWeight(1)
              .height(46)
              .type(this.showPassword ? InputType.Normal : InputType.Password)
              .fontSize(15)
              .fontColor('#222222')
              .placeholderColor('#A0A7B5')
              .backgroundColor(Color.Transparent)
              .padding({ left: 0, right: 0 })
              .onChange((value: string) => {
                this.password = value
              })

            Text(this.showPassword ? '隐藏' : '显示')
              .fontSize(12)
              .fontColor('#6B7280')
              .padding({ left: 10, right: 2 })
              .onClick(() => {
                this.showPassword = !this.showPassword
              })
          }
          .width('100%')
          .height(48)
          .padding({ left: 14, right: 14 })
          .borderRadius(14)
          .backgroundColor('#F6F8FC')
          .margin({ bottom: 20 })

          // 胶囊登录按钮
          Button(this.isLoggingIn ? '登录中...' : '立即登录')
            .width('100%')
            .height(50)
            .fontSize(17)
            .fontWeight(FontWeight.Bold)
            .fontColor('#FFFFFF')
            .backgroundColor('#2F68FF')
            .borderRadius(25)
            .enabled(!this.isLoggingIn)
            .shadow({
              radius: 16,
              color: '#332F68FF',
              offsetX: 0,
              offsetY: 8
            })
            .onClick(async () => {
              await this.handleLogin()
            })

          Row() {
            Row() {
              Text('默认账号')
                .fontSize(11)
                .fontColor('#A0A7B5')

              Text('admin')
                .fontSize(12)
                .fontWeight(FontWeight.Bold)
                .fontColor('#6B7280')
                .margin({ left: 5 })
            }

            Row() {
              Text('默认密码')
                .fontSize(11)
                .fontColor('#A0A7B5')

              Text('123456')
                .fontSize(12)
                .fontWeight(FontWeight.Bold)
                .fontColor('#6B7280')
                .margin({ left: 5 })
            }
          }
          .width('100%')
          .justifyContent(FlexAlign.SpaceBetween)
          .margin({ top: 14 })
        }
        .width('100%')
        .padding({ left: 22, right: 22, top: 22, bottom: 20 })
        .borderRadius(28)
        .backgroundColor('#FFFFFF')
        .shadow({
          radius: 24,
          color: '#16000000',
          offsetX: 0,
          offsetY: 10
        })

        // 底部轻说明
        Row() {
          Text('企业会议')
            .fontSize(13)
            .fontColor('#8A93A3')

          Text('·')
            .fontSize(13)
            .fontColor('#C6CBD5')
            .margin({ left: 8, right: 8 })

          Text('多人协作')
            .fontSize(13)
            .fontColor('#8A93A3')

          Text('·')
            .fontSize(13)
            .fontColor('#C6CBD5')
            .margin({ left: 8, right: 8 })

          Text('安全可靠')
            .fontSize(13)
            .fontColor('#8A93A3')
        }
        .margin({ top: 28 })
      }
      .width('100%')
      .padding({ left: 24, right: 24 })
      .position({ x: 0, y: 200 })
      .alignItems(HorizontalAlign.Center)
    }
    .width('100%')
    .height('100%')
  }

  @Builder
  private buildBrandTag(text: string) {
    Text(text)
      .fontSize(13)
      .fontColor('#2F68FF')
      .fontWeight(FontWeight.Bold)
      .padding({ left: 12, right: 12, top: 7, bottom: 7 })
      .backgroundColor('#EAF1FF')
      .borderRadius(16)
      .margin({ right: 10 })
  }

  @Builder
  private buildInputLabel(text: string) {
    Text(text)
      .width('100%')
      .fontSize(15)
      .fontWeight(FontWeight.Bold)
      .fontColor('#374151')
      .margin({ bottom: 9 })
  }

  private async handleLogin(): Promise<void> {
    if (this.isLoggingIn) {
      return
    }

    if (this.username.trim().length === 0 || this.password.trim().length === 0) {
      this.showToast('请输入账号和密码')
      return
    }

    this.isLoggingIn = true
    this.statusText = '正在登录，请稍候...'

    try {
      const result = await AuthApi.login(this.username.trim(), this.password.trim())
      console.info('[LoginPage][handleLogin] ' + JSON.stringify(result))

      if (result.code === 200) {
        this.statusText = '登录成功'
        this.showToast('登录成功')

        const param: LoginToHomeParam = new LoginToHomeParam()

        HMRouterMgr.replace({
          pageUrl: 'MeetingHomePage',
          param: param
        })
        return
      }

      this.statusText = '登录失败：' + result.msg
      this.showToast(result.msg)
    } catch (error) {
      console.error('[LoginPage][handleLogin] error=' + JSON.stringify(error))
      this.statusText = '登录异常，请检查网络'
      this.showToast('登录异常，请检查网络')
    } finally {
      this.isLoggingIn = false
    }
  }

  private showToast(message: string): void {
    promptAction.showToast({
      message: message,
      duration: 2000
    })
  }
}
