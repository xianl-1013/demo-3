import { MeetingHistoryResponse } from '../api/model/MeetingModels'

@Component
export struct MinePage {
  @Link historyRows: MeetingHistoryResponse[]

  currentUserName: string = '管理员'
  currentUserRole: string = '企业会议账号'
  currentUserPhone: string = '13800000000'

  onBackMeeting: () => void = () => {}
  onLogout: () => void = () => {}
  onOpenAccount: () => void = () => {}
  onOpenMeetingSetting: () => void = () => {}
  onOpenAbout: () => void = () => {}

  build() {
    Stack() {
      Image($r('app.media.glass_bg_mine'))
        .width('100%')
        .height('100%')
        .objectFit(ImageFit.Cover)

      Column() {
        this.buildMineHeader()

        Scroll() {
          Column() {
            this.buildMineProfileCard()
            this.buildMineDataCard()
            this.buildMineSettingCard()
            this.buildMineLogoutButton()
          }
          .width('100%')
          .padding({ left: 18, right: 18, bottom: 24 })
        }
        .layoutWeight(1)
      }
      .width('100%')
      .height('100%')
    }
  }

  @Builder
  private buildMineHeader() {
    Row() {
      Column() {
        Text('我的')
          .fontSize(28)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1F2937')

        Text('账号信息与会议设置')
          .fontSize(13)
          .fontColor('#8A93A3')
          .margin({ top: 6 })
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1)

      Text('会议')
        .fontSize(13)
        .fontColor('#2F68FF')
        .padding({ left: 14, right: 14, top: 8, bottom: 8 })
        .backgroundColor('#EAF1FF')
        .borderRadius(18)
        .onClick(() => {
          this.onBackMeeting()
        })
    }
    .width('100%')
    .padding({ left: 22, right: 22, top: 62, bottom: 18 })
  }

  @Builder
  private buildMineProfileCard() {
    Column() {
      Row() {
        Column() {
          Text('刘')
            .fontSize(30)
            .fontWeight(FontWeight.Bold)
            .fontColor('#FFFFFF')
        }
        .width(72)
        .height(72)
        .borderRadius(26)
        .backgroundColor('#2F68FF')
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
        .shadow({
          radius: 16,
          color: '#302F68FF',
          offsetX: 0,
          offsetY: 8
        })

        Column() {
          Text(this.currentUserName)
            .fontSize(22)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1F2937')

          Text(this.currentUserRole)
            .fontSize(13)
            .fontColor('#7B8494')
            .margin({ top: 6 })

          Text(this.currentUserPhone)
            .fontSize(12)
            .fontColor('#A0A7B5')
            .margin({ top: 4 })
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1)
        .margin({ left: 16 })
      }
      .width('100%')

      Row() {
        this.buildProfileTag('企业版')
        this.buildProfileTag('高清视频')
        this.buildProfileTag('安全会议')
      }
      .width('100%')
      .margin({ top: 18 })
    }
    .width('100%')
    .padding(20)
    .borderRadius(28)
    .backgroundColor('#E6FFFFFF')
    .shadow({
      radius: 22,
      color: '#22000000',
      offsetX: 0,
      offsetY: 8
    })
  }

  @Builder
  private buildProfileTag(text: string) {
    Text(text)
      .fontSize(12)
      .fontColor('#2F68FF')
      .fontWeight(FontWeight.Bold)
      .padding({ left: 10, right: 10, top: 6, bottom: 6 })
      .backgroundColor('#EAF1FF')
      .borderRadius(15)
      .margin({ right: 9 })
  }

  @Builder
  private buildMineDataCard() {
    Row() {
      this.buildMineDataItem(String(this.historyRows.length), '会议记录')
      this.buildMineDataItem(String(this.getRunningCount()), '进行中')
      this.buildMineDataItem(String(this.getEndedCount()), '已结束')
    }
    .width('100%')
    .padding({ top: 18, bottom: 18 })
    .margin({ top: 14 })
    .borderRadius(28)
    .backgroundColor('#E6FFFFFF')
    .shadow({
      radius: 22,
      color: '#22000000',
      offsetX: 0,
      offsetY: 8
    })
  }

  @Builder
  private buildMineDataItem(value: string, label: string) {
    Column() {
      Text(value)
        .fontSize(22)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1F2937')

      Text(label)
        .fontSize(12)
        .fontColor('#8A93A3')
        .margin({ top: 5 })
    }
    .layoutWeight(1)
    .alignItems(HorizontalAlign.Center)
  }

  @Builder
  private buildMineSettingCard() {
    Column() {
      this.buildMineMenuItem('账号信息', '查看登录账号与身份信息', () => {
        this.onOpenAccount()
      })

      Divider()
        .width('100%')
        .height(1)
        .color('#EEF1F6')
        .margin({ left: 54 })

      this.buildMineMenuItem('会议设置', '默认麦克风、摄像头、入会配置', () => {
        this.onOpenMeetingSetting()
      })

      Divider()
        .width('100%')
        .height(1)
        .color('#EEF1F6')
        .margin({ left: 54 })

      this.buildMineMenuItem('关于智云会议', '版本信息与服务说明', () => {
        this.onOpenAbout()
      })
    }
    .width('100%')
    .padding({ left: 16, right: 16 })
    .margin({ top: 14 })
    .borderRadius(28)
    .backgroundColor('#E6FFFFFF')
    .shadow({
      radius: 22,
      color: '#22000000',
      offsetX: 0,
      offsetY: 8
    })
  }

  @Builder
  private buildMineMenuItem(title: string, desc: string, action: () => void) {
    Row() {
      Column() {
        Text(title.substring(0, 1))
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#2F68FF')
      }
      .width(42)
      .height(42)
      .borderRadius(16)
      .backgroundColor('#EAF1FF')
      .justifyContent(FlexAlign.Center)
      .alignItems(HorizontalAlign.Center)

      Column() {
        Text(title)
          .fontSize(15)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1F2937')

        Text(desc)
          .fontSize(12)
          .fontColor('#8A93A3')
          .margin({ top: 4 })
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1)
      .margin({ left: 12 })

      Text('>')
        .fontSize(18)
        .fontColor('#C0C6D2')
    }
    .width('100%')
    .height(70)
    .onClick(() => {
      action()
    })
  }

  @Builder
  private buildMineLogoutButton() {
    Button('退出登录')
      .width('100%')
      .height(50)
      .fontSize(16)
      .fontWeight(FontWeight.Bold)
      .fontColor('#E5484D')
      .backgroundColor('#FFF0F0')
      .borderRadius(25)
      .margin({ top: 18 })
      .onClick(() => {
        this.onLogout()
      })
  }

  private getRunningCount(): number {
    let count: number = 0

    for (let i = 0; i < this.historyRows.length; i++) {
      if (this.historyRows[i].status === 'running') {
        count++
      }
    }

    return count
  }

  private getEndedCount(): number {
    let count: number = 0

    for (let i = 0; i < this.historyRows.length; i++) {
      if (this.historyRows[i].status !== 'running') {
        count++
      }
    }

    return count
  }
}
