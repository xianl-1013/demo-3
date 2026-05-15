import { MeetingHistoryResponse } from '../api/model/MeetingModels'

@Component
export struct MeetingTabPage {
  @Link meetingTitle: string
  @Link joinMeetingNo: string
  @Link statusText: string
  @Link historyRows: MeetingHistoryResponse[]
  @Link isLoadingHistory: boolean
  @Link showJoinPanel: boolean
  @Link showCreatePanel: boolean

  onCreateMeeting: () => void = () => {}
  onJoinMeeting: () => void = () => {}
  onLoadHistory: () => void = () => {}
  onClickHistoryMeeting: (item: MeetingHistoryResponse) => void = (item: MeetingHistoryResponse) => {}
  onOpenMine: () => void = () => {}

  build() {
    Column() {
      this.buildMeetingHeader()

      Scroll() {
        Column() {
          this.buildMainActionCard()

          if (this.showJoinPanel) {
            this.buildJoinPanel()
          }

          if (this.showCreatePanel) {
            this.buildCreatePanel()
          }

          this.buildTodayMeetingCard()
          this.buildRecentHistoryCard()
        }
        .width('100%')
        .padding({ left: 18, right: 18, bottom: 24 })
      }
      .layoutWeight(1)
    }
    .width('100%')
    .height('100%')
    .padding({ bottom: 74 })
  }

  @Builder
  private buildMeetingHeader() {
    Column() {
      Row() {
        Column() {
          Text('刘')
            .fontSize(20)
            .fontWeight(FontWeight.Bold)
            .fontColor('#FFFFFF')
        }
        .width(50)
        .height(50)
        .borderRadius(18)
        .backgroundColor('#2F68FF')
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
        .shadow({
          radius: 14,
          color: '#302F68FF',
          offsetX: 0,
          offsetY: 7
        })
        .onClick(() => {
          this.onOpenMine()
        })

        Column() {
          Text(this.getHelloText())
            .fontSize(14)
            .fontColor('#8A93A3')

          Text('智云会议')
            .fontSize(27)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1F2937')
            .margin({ top: 3 })
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1)
        .margin({ left: 12 })

        Text('刷新')
          .fontSize(13)
          .fontColor('#2F68FF')
          .padding({ left: 14, right: 14, top: 8, bottom: 8 })
          .backgroundColor('#EAF1FF')
          .borderRadius(18)
          .onClick(() => {
            this.onLoadHistory()
          })
      }
      .width('100%')

      Text(this.statusText)
        .width('100%')
        .fontSize(13)
        .fontColor('#7B8494')
        .margin({ top: 12 })
    }
    .width('100%')
    .padding({ left: 22, right: 22, top: 58, bottom: 18 })
  }

  @Builder
  private buildMainActionCard() {
    Column() {
      Row() {
        this.buildMainActionItem('+', '加入会议', '输入会议号', () => {
          this.showJoinPanel = !this.showJoinPanel
          this.showCreatePanel = false
        })

        this.buildMainActionItem('▶', '快速会议', '立即开会', () => {
          this.meetingTitle = '即时会议'
          this.onCreateMeeting()
        })

        this.buildMainActionItem('日', '预约会议', '后续接入', () => {
          this.showCreatePanel = !this.showCreatePanel
          this.showJoinPanel = false
        })

        this.buildMainActionItem('历', '历史会议', '最近记录', () => {
          this.onLoadHistory()
        })
      }
      .width('100%')
      .justifyContent(FlexAlign.SpaceBetween)
    }
    .width('100%')
    .padding({ left: 14, right: 14, top: 18, bottom: 18 })
    .borderRadius(28)
    .backgroundColor('#FFFFFF')
    .shadow({
      radius: 18,
      color: '#12000000',
      offsetX: 0,
      offsetY: 8
    })
  }

  @Builder
  private buildMainActionItem(iconText: string, title: string, desc: string, action: () => void) {
    Column() {
      Column() {
        Text(iconText)
          .fontSize(22)
          .fontWeight(FontWeight.Bold)
          .fontColor('#FFFFFF')
      }
      .width(56)
      .height(56)
      .borderRadius(20)
      .backgroundColor('#2F68FF')
      .justifyContent(FlexAlign.Center)
      .alignItems(HorizontalAlign.Center)
      .shadow({
        radius: 12,
        color: '#262F68FF',
        offsetX: 0,
        offsetY: 6
      })

      Text(title)
        .fontSize(13)
        .fontWeight(FontWeight.Bold)
        .fontColor('#222222')
        .margin({ top: 8 })
        .maxLines(1)

      Text(desc)
        .fontSize(11)
        .fontColor('#9CA3AF')
        .margin({ top: 3 })
        .maxLines(1)
    }
    .width('24%')
    .alignItems(HorizontalAlign.Center)
    .onClick(() => {
      action()
    })
  }

  @Builder
  private buildJoinPanel() {
    Column() {
      Row() {
        Text('加入会议')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1F2937')
          .layoutWeight(1)

        Text('收起')
          .fontSize(13)
          .fontColor('#8A93A3')
          .onClick(() => {
            this.showJoinPanel = false
          })
      }
      .width('100%')
      .margin({ bottom: 12 })

      Row() {
        TextInput({ placeholder: '请输入会议号', text: this.joinMeetingNo })
          .layoutWeight(1)
          .height(48)
          .fontSize(15)
          .fontColor('#222222')
          .placeholderColor('#A0A7B5')
          .backgroundColor('#F6F8FC')
          .borderRadius(24)
          .padding({ left: 16, right: 16 })
          .onChange((value: string) => {
            this.joinMeetingNo = value
          })

        Button('加入')
          .width(88)
          .height(48)
          .fontSize(15)
          .fontWeight(FontWeight.Bold)
          .fontColor('#FFFFFF')
          .backgroundColor('#2F68FF')
          .borderRadius(24)
          .margin({ left: 10 })
          .onClick(() => {
            this.onJoinMeeting()
          })
      }
      .width('100%')
    }
    .width('100%')
    .padding(18)
    .margin({ top: 14 })
    .borderRadius(26)
    .backgroundColor('#FFFFFF')
    .shadow({
      radius: 16,
      color: '#10000000',
      offsetX: 0,
      offsetY: 8
    })
  }

  @Builder
  private buildCreatePanel() {
    Column() {
      Row() {
        Text('预约 / 发起会议')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1F2937')
          .layoutWeight(1)

        Text('收起')
          .fontSize(13)
          .fontColor('#8A93A3')
          .onClick(() => {
            this.showCreatePanel = false
          })
      }
      .width('100%')
      .margin({ bottom: 12 })

      TextInput({ placeholder: '请输入会议标题', text: this.meetingTitle })
        .width('100%')
        .height(48)
        .fontSize(15)
        .fontColor('#222222')
        .placeholderColor('#A0A7B5')
        .backgroundColor('#F6F8FC')
        .borderRadius(24)
        .padding({ left: 16, right: 16 })
        .margin({ bottom: 12 })
        .onChange((value: string) => {
          this.meetingTitle = value
        })

      Button('立即发起')
        .width('100%')
        .height(48)
        .fontSize(16)
        .fontWeight(FontWeight.Bold)
        .fontColor('#FFFFFF')
        .backgroundColor('#2F68FF')
        .borderRadius(24)
        .onClick(() => {
          this.onCreateMeeting()
        })
    }
    .width('100%')
    .padding(18)
    .margin({ top: 14 })
    .borderRadius(26)
    .backgroundColor('#FFFFFF')
    .shadow({
      radius: 16,
      color: '#10000000',
      offsetX: 0,
      offsetY: 8
    })
  }

  @Builder
  private buildTodayMeetingCard() {
    Column() {
      Row() {
        Column() {
          Text('今日')
            .fontSize(22)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1F2937')

          Text(this.getTodaySubText())
            .fontSize(13)
            .fontColor('#8A93A3')
            .margin({ top: 5 })
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1)

        Text(this.getTodayTagText())
          .fontSize(13)
          .fontWeight(FontWeight.Bold)
          .fontColor(this.getTodayTagColor())
          .padding({ left: 12, right: 12, top: 6, bottom: 6 })
          .backgroundColor(this.getTodayTagBgColor())
          .borderRadius(15)
      }
      .width('100%')

      Column() {
        Text(this.historyRows.length === 0 ? '☕' : '📹')
          .fontSize(42)

        Text(this.historyRows.length === 0 ? '暂无会议' : '会议记录已更新')
          .fontSize(16)
          .fontWeight(FontWeight.Bold)
          .fontColor('#7B8494')
          .margin({ top: 8 })

        Text(this.historyRows.length === 0 ? '创建或加入会议后会显示在这里' : '可在下方查看最近会议')
          .fontSize(13)
          .fontColor('#A0A7B5')
          .margin({ top: 5 })
      }
      .width('100%')
      .height(132)
      .justifyContent(FlexAlign.Center)
      .alignItems(HorizontalAlign.Center)
    }
    .width('100%')
    .padding(18)
    .margin({ top: 14 })
    .borderRadius(28)
    .backgroundColor('#FFFFFF')
    .shadow({
      radius: 18,
      color: '#10000000',
      offsetX: 0,
      offsetY: 8
    })
  }

  @Builder
  private buildRecentHistoryCard() {
    Column() {
      Row() {
        Text('最近会议')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1F2937')
          .layoutWeight(1)

        Text(this.isLoadingHistory ? '刷新中' : '全部')
          .fontSize(13)
          .fontColor('#2F68FF')
          .padding({ left: 12, right: 12, top: 6, bottom: 6 })
          .backgroundColor('#EAF1FF')
          .borderRadius(15)
          .onClick(() => {
            this.onLoadHistory()
          })
      }
      .width('100%')
      .margin({ bottom: 12 })

      if (this.historyRows.length === 0) {
        Text('暂无会议记录')
          .fontSize(14)
          .fontColor('#9CA3AF')
          .width('100%')
          .height(54)
          .textAlign(TextAlign.Center)
      } else {
        ForEach(this.getHomeHistoryRows(), (item: MeetingHistoryResponse) => {
          this.buildHomeHistoryItem(item)
        }, (item: MeetingHistoryResponse) => {
          return item.meetingId + '_' + item.status + '_' + String(item.online)
        })
      }
    }
    .width('100%')
    .padding(18)
    .margin({ top: 14 })
    .borderRadius(28)
    .backgroundColor('#FFFFFF')
    .shadow({
      radius: 18,
      color: '#10000000',
      offsetX: 0,
      offsetY: 8
    })
  }

  @Builder
  private buildHomeHistoryItem(item: MeetingHistoryResponse) {
    Row() {
      Column() {
        Text(item.title)
          .fontSize(16)
          .fontWeight(FontWeight.Bold)
          .fontColor('#222222')
          .maxLines(1)
          .textOverflow({ overflow: TextOverflow.Ellipsis })

        Text('会议号：' + item.meetingNo)
          .fontSize(13)
          .fontColor('#7B8494')
          .margin({ top: 5 })

        Text('开始时间：' + item.startTime)
          .fontSize(12)
          .fontColor('#A0A7B5')
          .margin({ top: 4 })
          .maxLines(1)
          .textOverflow({ overflow: TextOverflow.Ellipsis })
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1)

      Column() {
        Text(this.getHistoryStatusText(item))
          .fontSize(12)
          .fontWeight(FontWeight.Bold)
          .fontColor(this.getHistoryStatusColor(item))
          .padding({ left: 10, right: 10, top: 5, bottom: 5 })
          .backgroundColor(this.getHistoryStatusBgColor(item))
          .borderRadius(12)

        Text(item.host ? '主持人' : '参会人')
          .fontSize(12)
          .fontColor('#8A93A3')
          .margin({ top: 8 })
      }
      .alignItems(HorizontalAlign.End)
    }
    .width('100%')
    .padding(14)
    .margin({ bottom: 10 })
    .borderRadius(18)
    .backgroundColor('#F6F8FC')
    .onClick(() => {
      this.onClickHistoryMeeting(item)
    })
  }

  private getHelloText(): string {
    const hour: number = new Date().getHours()

    if (hour < 6) {
      return '夜深了'
    }

    if (hour < 12) {
      return '上午好'
    }

    if (hour < 18) {
      return '下午好'
    }

    return '晚上好'
  }

  private getTodaySubText(): string {
    if (this.historyRows.length === 0) {
      return '暂无进行中的会议'
    }

    return '最近会议已同步'
  }

  private getTodayTagText(): string {
    if (this.historyRows.length === 0) {
      return '空闲'
    }

    return '查看'
  }

  private getTodayTagColor(): string {
    if (this.historyRows.length === 0) {
      return '#8A93A3'
    }

    return '#2F68FF'
  }

  private getTodayTagBgColor(): string {
    if (this.historyRows.length === 0) {
      return '#EEF1F6'
    }

    return '#EAF1FF'
  }

  private getHomeHistoryRows(): MeetingHistoryResponse[] {
    const rows: MeetingHistoryResponse[] = []
    const maxCount: number = this.historyRows.length > 2 ? 2 : this.historyRows.length

    for (let i = 0; i < maxCount; i++) {
      rows.push(this.historyRows[i])
    }

    return rows
  }

  private getHistoryStatusText(item: MeetingHistoryResponse): string {
    if (item.status === 'running') {
      if (item.online) {
        return '会议中'
      }

      return '已离开'
    }

    return '已结束'
  }

  private getHistoryStatusColor(item: MeetingHistoryResponse): string {
    if (item.status === 'running' && item.online) {
      return '#0EA05A'
    }

    if (item.status === 'running' && !item.online) {
      return '#F59E0B'
    }

    return '#8A93A3'
  }

  private getHistoryStatusBgColor(item: MeetingHistoryResponse): string {
    if (item.status === 'running' && item.online) {
      return '#E8F8F0'
    }

    if (item.status === 'running' && !item.online) {
      return '#FFF4DE'
    }

    return '#EEF1F6'
  }
}