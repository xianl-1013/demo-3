import {
  AbilityConstant,
  ConfigurationConstant,
  UIAbility,
  Want
} from '@kit.AbilityKit'
import { hilog } from '@kit.PerformanceAnalysisKit'
import { KeyboardAvoidMode, window } from '@kit.ArkUI'
import { HMRouterMgr } from '@hadss/hmrouter'
import { BusinessError } from '@kit.BasicServicesKit'

const DOMAIN = 0x0000

export default class EntryAbility extends UIAbility {
  private uiContext: UIContext | undefined = undefined

  async onCreate(want: Want, launchParam: AbilityConstant.LaunchParam): Promise<void> {
    hilog.info(DOMAIN, 'testTag', '%{public}s', 'Ability onCreate')

    try {
      this.context.getApplicationContext().setColorMode(
        ConfigurationConstant.ColorMode.COLOR_MODE_NOT_SET
      )
    } catch (err) {
      hilog.error(
        DOMAIN,
        'testTag',
        'Failed to set colorMode. Cause: %{public}s',
        JSON.stringify(err)
      )
    }

    try {
      HMRouterMgr.openLog('INFO')

      const routerItems = await HMRouterMgr.init({
        context: this.context
      })

      HMRouterMgr.setDefaultNavigationId('MainNavigation')

      console.info('[EntryAbility] HMRouter init success')
      console.info('[EntryAbility] routerItems=' + JSON.stringify(routerItems))
      console.info('[EntryAbility] exist LoginPage=' + HMRouterMgr.existUrl('LoginPage'))
      console.info('[EntryAbility] exist MeetingHomePage=' + HMRouterMgr.existUrl('MeetingHomePage'))
      console.info('[EntryAbility] exist MeetingRoomPage=' + HMRouterMgr.existUrl('MeetingRoomPage'))
    } catch (error) {
      console.error('[EntryAbility] HMRouter init failed=' + JSON.stringify(error))
    }
  }

  onDestroy(): void {
    hilog.info(DOMAIN, 'testTag', '%{public}s', 'Ability onDestroy')
  }

  onWindowStageCreate(windowStage: window.WindowStage): void {
    hilog.info(DOMAIN, 'testTag', '%{public}s', 'Ability onWindowStageCreate')

    // 只加载 HMRouter 根容器页面
    // LoginPage / MeetingHomePage / MeetingRoomPage 不要在这里 loadContent
    windowStage.loadContent('pages/Index', (err) => {
      if (err.code) {
        hilog.error(
          DOMAIN,
          'testTag',
          'Failed to load pages/Index. Cause: %{public}s',
          JSON.stringify(err)
        )
        return
      }

      hilog.info(DOMAIN, 'testTag', '%{public}s', 'Succeeded in loading pages/Index')

      try {
        this.uiContext = windowStage.getMainWindowSync().getUIContext()
        this.uiContext.setKeyboardAvoidMode(KeyboardAvoidMode.RESIZE)
        AppStorage.setOrCreate('uiContext', this.uiContext)

        const windowClass: window.Window = windowStage.getMainWindowSync()

        windowClass.setWindowLayoutFullScreen(true).then(() => {
          let type = window.AvoidAreaType.TYPE_NAVIGATION_INDICATOR
          let avoidArea = windowClass.getWindowAvoidArea(type)
          let bottomRectHeight = windowClass.getUIContext().px2vp(avoidArea.bottomRect.height)
          AppStorage.setOrCreate('bottomRectHeight', bottomRectHeight)

          type = window.AvoidAreaType.TYPE_SYSTEM
          avoidArea = windowClass.getWindowAvoidArea(type)
          let topRectHeight = windowClass.getUIContext().px2vp(avoidArea.topRect.height)
          AppStorage.setOrCreate('topRectHeight', topRectHeight)
        }).catch((error: BusinessError) => {
          hilog.error(
            DOMAIN,
            'testTag',
            'setWindowLayoutFullScreen failed. code=%{public}d, message=%{public}s',
            error.code,
            error.message
          )
        })

        windowClass.on('avoidAreaChange', (data) => {
          if (data.type === window.AvoidAreaType.TYPE_SYSTEM) {
            let topRectHeight = windowClass.getUIContext().px2vp(data.area.topRect.height)
            AppStorage.setOrCreate('topRectHeight', topRectHeight)
          } else if (data.type === window.AvoidAreaType.TYPE_NAVIGATION_INDICATOR) {
            let bottomRectHeight = windowClass.getUIContext().px2vp(data.area.bottomRect.height)
            AppStorage.setOrCreate('bottomRectHeight', bottomRectHeight)
          }
        })
      } catch (error) {
        const err = error as BusinessError
        hilog.error(
          DOMAIN,
          'testTag',
          'Window init failed. code=%{public}d, message=%{public}s',
          err.code,
          err.message
        )
      }
    })
  }

  onWindowStageDestroy(): void {
    hilog.info(DOMAIN, 'testTag', '%{public}s', 'Ability onWindowStageDestroy')
  }

  onForeground(): void {
    hilog.info(DOMAIN, 'testTag', '%{public}s', 'Ability onForeground')
  }

  onBackground(): void {
    hilog.info(DOMAIN, 'testTag', '%{public}s', 'Ability onBackground')
  }
}