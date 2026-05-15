import {
  HMNavigation,
  HMDefaultGlobalAnimator
} from '@hadss/hmrouter'
import { AttributeUpdater } from '@kit.ArkUI'

@Entry
@Component
struct Index {
  private modifier: MyNavModifier = new MyNavModifier()

  build() {
    Column() {
      HMNavigation({
        navigationId: 'MainNavigation',
        homePageUrl: 'LoginPage',
        options: {
          standardAnimator: HMDefaultGlobalAnimator.STANDARD_ANIMATOR,
          dialogAnimator: HMDefaultGlobalAnimator.DIALOG_ANIMATOR,
          modifier: this.modifier
        }
      })
    }
    .width('100%')
    .height('100%')
  }
}

class MyNavModifier extends AttributeUpdater<NavigationAttribute> {
  initializeModifier(instance: NavigationAttribute): void {
    instance.hideNavBar(true)
  }
}
