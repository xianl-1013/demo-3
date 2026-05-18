import promptAction from '@ohos.promptAction'
import { HMRouter, HMRouterMgr } from '@hadss/hmrouter'
import { AuthApi } from '../api/AuthApi'
import { MeetingApi } from '../api/MeetingApi'
import {
  CreateMeetingResponse,
  JoinMeetingResponse,
  MeetingHistoryResponse,
  MeetingInfoResponse
} from '../api/model/MeetingModels'
import { MeetingRuntimeStore } from '../utils/MeetingRuntimeStore'
import { MeetingBackParam } from '../utils/MeetingBackParam'
import { MeetingTabPage } from './MeetingTabPage'
import { ContactPage } from './ContactPage'
import { MinePage } from './MinePage'
@HMRouter({
  pageUrl: 'MeetingHomePage'
})
@Component
export struct MeetingHomePage {
  @State meetingTitle: string = '即时会议'
  @State joinMeetingNo: string = ''
  @State statusText: string = '欢迎使用智云会议'
  @State historyRows: MeetingHistoryResponse[] = []
  @State isLoadingHistory: boolean = false
  @State lastBackParamTime: number = 0

  @State showJoinPanel: boolean = false
  @State showCreatePanel: boolean = false

  // 0：会议，1：通讯录，2：我的
  @State currentTab: number = 0
  @State showBottomLiquid: boolean = false
  @State bottomLiquidTab: number = 0

  // 我的页面基础信息，后面可以从 AuthApi 或用户接口中读取
  @State currentUserName: string = '管理员'
  @State currentUserRole: string = '企业会议账号'
  @State currentUserPhone: string = '13800000000'

  private lastResumeRefreshTime: number = 0
  private bottomLiquidAnimationVersion: number = 0

  private log(tag: string, message: string): void {
    console.info('[MeetingHomePage][' + tag + '] ' + message)
  }

  private logObj(tag: string, label: string, value: Object | null | undefined): void {
    try {
      console.info('[MeetingHomePage][' + tag + '] ' + label + '=' + JSON.stringify(value))
    } catch (error) {
      console.info('[MeetingHomePage][' + tag + '] ' + label + '=JSON.stringify failed')
    }
  }

  aboutToAppear(): void {
    this.log('aboutToAppear', '页面进入')
    this.handleBackParamAndRefresh('aboutToAppear')
  }

  onPageShow(): void {
    this.log('onPageShow', '页面显示')
    this.handleBackParamAndRefresh('onPageShow')
  }

  build() {
    Stack() {
      Column()
        .width('100%')
        .height('100%')
        .backgroundColor('#FFFFFF')

      Column() {
        Stack() {
          if (this.currentTab === 0) {
            MeetingTabPage({
              meetingTitle: $meetingTitle,
              joinMeetingNo: $joinMeetingNo,
              statusText: $statusText,
              historyRows: $historyRows,
              isLoadingHistory: $isLoadingHistory,
              showJoinPanel: $showJoinPanel,
              showCreatePanel: $showCreatePanel,
              onCreateMeeting: () => {
                this.handleCreateMeeting()
              },
              onJoinMeeting: () => {
                this.handleJoinMeeting()
              },
              onLoadHistory: () => {
                this.loadHistory(true)
              },
              onClickHistoryMeeting: (item: MeetingHistoryResponse) => {
                this.handleClickHistoryMeeting(item)
              },
              onOpenMine: () => {
                this.currentTab = 2
              }
            })
          } else if (this.currentTab === 1) {
            ContactPage({
              onCreateGroupMeeting: () => {
                this.currentTab = 0
                this.showCreatePanel = true
                this.showJoinPanel = false
                this.statusText = '发起群组会议'
              },
              onInviteMember: () => {
                this.showToast('邀请成员后续接入')
              },
              onOpenOrganization: () => {
                this.showToast('组织架构后续接入')
              },
              onStartContactMeeting: (userId: string) => {
                this.currentTab = 0
                this.meetingTitle = '联系人会议'
                this.showToast('正在准备与用户 ' + userId + ' 发起会议')
              }
            })
          } else {
            MinePage({
              historyRows: $historyRows,
              currentUserName: '管理员',
              currentUserRole: '企业会议账号',
              currentUserPhone: '13800000000',
              onBackMeeting: () => {
                this.currentTab = 0
              },
              onLogout: () => {
                AuthApi.logout()
                MeetingRuntimeStore.clear()

                HMRouterMgr.replace({
                  pageUrl: 'LoginPage'
                })
              },
              onOpenAccount: () => {
                this.showToast('账号信息后续接入')
              },
              onOpenMeetingSetting: () => {
                this.showToast('会议设置后续接入')
              },
              onOpenAbout: () => {
                this.showToast('当前版本 V1.0.0')
              }
            })
          }
        }
        .width('100%')
        .layoutWeight(1)

        this.buildBottomBar()
      }
      .width('100%')
      .height('100%')
    }
    .width('100%')
    .height('100%')
  }

  @Builder
  private buildBottomBar() {
    Column() {
      Stack() {
        if (this.showBottomLiquid) {
          Column()
            .width('30%')
            .height(46)
            .borderRadius(23)
            .backgroundColor('#42FFFFFF')
            .backdropBlur(24)
            .border({
              width: 1,
              color: '#78FFFFFF'
            })
            .shadow({
              radius: 16,
              color: '#243B82FF',
              offsetX: 0,
              offsetY: 5
            })
            .position({ x: this.getBottomLiquidLeft(this.bottomLiquidTab), y: 13 })
            .animation({
              duration: 320,
              curve: Curve.EaseInOut
            })
        }

        Row() {
          this.buildBottomTabItem(0, '会议')
          this.buildBottomTabItem(1, '通讯录')
          this.buildBottomTabItem(2, '我的')
        }
        .width('100%')
        .height('100%')
        .justifyContent(FlexAlign.SpaceAround)
      }
      .width('100%')
      .height(72)
      .borderRadius(30)
      .backgroundColor('#64FFFFFF')
      .backdropBlur(28)
      .border({
        width: 1,
        color: '#8CFFFFFF'
      })
      .shadow({
        radius: 22,
        color: '#1F2F68FF',
        offsetX: 0,
        offsetY: 8
      })
      .clip(true)
    }
    .width('100%')
    .height(84)
    .padding({ left: 14, right: 14, top: 4, bottom: 8 })
  }

  private getBottomLiquidLeft(tab: number): string {
    if (tab === 0) {
      return '2%'
    }

    if (tab === 1) {
      return '35%'
    }

    return '68%'
  }

  private handleBottomTabClick(index: number): void {
    if (this.currentTab === index) {
      return
    }

    this.bottomLiquidAnimationVersion++
    const animationVersion: number = this.bottomLiquidAnimationVersion

    this.bottomLiquidTab = this.currentTab
    this.showBottomLiquid = true
    this.currentTab = index

    setTimeout(() => {
      if (this.bottomLiquidAnimationVersion === animationVersion) {
        this.bottomLiquidTab = index
      }
    }, 20)

    setTimeout(() => {
      if (this.bottomLiquidAnimationVersion === animationVersion) {
        this.showBottomLiquid = false
      }
    }, 390)
  }

  /**
   * 会议首页
   */
  @Builder
  private buildMeetingPage() {
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

  }

  @Builder
  private buildMeetingHeader() {
    Column() {
      Row() {
        // 头像
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
          this.currentTab = 1
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

        Column() {
          Text('我的')
            .fontSize(13)
            .fontColor('#2F68FF')
        }
        .height(34)
        .padding({ left: 14, right: 14 })
        .borderRadius(17)
        .backgroundColor('#EAF1FF')
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
        .onClick(() => {
          this.currentTab = 1
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

  /**
   * 会议页四个入口
   */
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
          this.handleCreateMeeting()
        })

        this.buildMainActionItem('日', '预约会议', '后续接入', () => {
          this.showCreatePanel = !this.showCreatePanel
          this.showJoinPanel = false
        })

        this.buildMainActionItem('历', '历史会议', '最近记录', () => {
          this.loadHistory(true)
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

  /**
   * 加入会议展开卡片
   */
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
            this.handleJoinMeeting()
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

  /**
   * 发起会议展开卡片
   */
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
          this.handleCreateMeeting()
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

  /**
   * 今日会议状态
   */
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

  /**
   * 最近会议，只展示两条，完整历史后续单独页面
   */
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
            this.loadHistory(true)
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
      this.handleClickHistoryMeeting(item)
    })
  }

  /**
   * 我的页面
   */
  @Builder
  private buildMinePage() {
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
          this.currentTab = 0
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
    .backgroundColor('#FFFFFF')
    .shadow({
      radius: 18,
      color: '#10000000',
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
    .backgroundColor('#FFFFFF')
    .shadow({
      radius: 18,
      color: '#10000000',
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
        this.showToast('账号信息后续接入')
      })

      Divider()
        .width('100%')
        .height(1)
        .color('#EEF1F6')
        .margin({ left: 54 })

      this.buildMineMenuItem('会议设置', '默认麦克风、摄像头、入会配置', () => {
        this.showToast('会议设置后续接入')
      })

      Divider()
        .width('100%')
        .height(1)
        .color('#EEF1F6')
        .margin({ left: 54 })

      this.buildMineMenuItem('关于智云会议', '版本信息与服务说明', () => {
        this.showToast('当前版本 V1.0.0')
      })
    }
    .width('100%')
    .padding({ left: 16, right: 16 })
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
        AuthApi.logout()
        MeetingRuntimeStore.clear()

        HMRouterMgr.replace({
          pageUrl: 'LoginPage'
        })
      })
  }



  @Builder
  private buildBottomTabItem(index: number, title: string) {
    Column() {
      if (index === 0) {
        this.buildMeetingTabIcon(this.currentTab === index)
      } else if (index === 1) {
        this.buildContactTabIcon(this.currentTab === index)
      } else {
        this.buildMineTabIcon(this.currentTab === index)
      }

      Text(title)
        .fontSize(12)
        .fontWeight(this.currentTab === index ? FontWeight.Bold : FontWeight.Normal)
        .fontColor(this.currentTab === index ? '#0A6CFF' : '#8D94A3')
        .margin({ top: 3 })
    }
    .layoutWeight(1)
    .height('100%')
    .justifyContent(FlexAlign.Center)
    .alignItems(HorizontalAlign.Center)
    .opacity(this.currentTab === index ? 1 : 0.86)
    .animation({
      duration: 240,
      curve: Curve.EaseInOut
    })
    .onClick(() => {
      this.handleBottomTabClick(index)
    })
  }
  @Builder
  private buildMeetingTabIcon(active: boolean) {
    if (active) {
      Image($r('app.media.tab_meeting_active'))
        .width(28)
        .height(28)
        .objectFit(ImageFit.Contain)
    } else {
      Image($r('app.media.tab_meeting_inactive'))
        .width(28)
        .height(28)
        .objectFit(ImageFit.Contain)
    }
  }

  @Builder
  private buildContactTabIcon(active: boolean) {
    if (active) {
      Image($r('app.media.tab_contact_active'))
        .width(28)
        .height(28)
        .objectFit(ImageFit.Contain)
    } else {
      Image($r('app.media.tab_contact_inactive'))
        .width(28)
        .height(28)
        .objectFit(ImageFit.Contain)
    }
  }

  @Builder
  private buildMineTabIcon(active: boolean) {
    if (active) {
      Image($r('app.media.tab_mine_active'))
        .width(28)
        .height(28)
        .objectFit(ImageFit.Contain)
    } else {
      Image($r('app.media.tab_mine_inactive'))
        .width(28)
        .height(28)
        .objectFit(ImageFit.Contain)
    }
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

  private handleBackParamAndRefresh(source: string): void {
    const nowTime: number = Date.now()

    this.log('handleBackParamAndRefresh',
      '触发 source=' + source +
        ', now=' + String(nowTime) +
        ', lastResumeRefreshTime=' + String(this.lastResumeRefreshTime) +
        ', lastBackParamTime=' + String(this.lastBackParamTime)
    )

    let hasBackParam: boolean = false

    try {
      const rawParam: Object | null | undefined =
        HMRouterMgr.getCurrentParam() as Object | null | undefined

      this.logObj('handleBackParamAndRefresh', 'rawParam', rawParam)

      if (rawParam !== null && rawParam !== undefined) {
        const param: MeetingBackParam = rawParam as MeetingBackParam

        const fromMeetingRoom: boolean = param.fromMeetingRoom === true
        const needRefresh: boolean = param.refresh === true
        const timestamp: number = param.timestamp ? Number(param.timestamp) : 0

        this.log('handleBackParamAndRefresh',
          '解析参数 fromMeetingRoom=' + String(fromMeetingRoom) +
            ', needRefresh=' + String(needRefresh) +
            ', timestamp=' + String(timestamp) +
            ', lastBackParamTime=' + String(this.lastBackParamTime)
        )

        if (fromMeetingRoom && needRefresh && timestamp > this.lastBackParamTime) {
          hasBackParam = true
          this.lastBackParamTime = timestamp

          this.log('handleBackParamAndRefresh', '命中会议返回参数，准备 applyMeetingBackParam')
          this.applyMeetingBackParam(param)
        } else {
          this.log('handleBackParamAndRefresh', '未命中会议返回参数')
        }
      }
    } catch (error) {
      console.error('[MeetingHomePage][handleBackParamAndRefresh] parse param error=' + JSON.stringify(error))
    }

    if (!hasBackParam && nowTime - this.lastResumeRefreshTime < 200) {
      this.log('handleBackParamAndRefresh', '无返回参数且 200ms 内重复触发，跳过刷新')
      return
    }

    this.lastResumeRefreshTime = nowTime

    this.log('handleBackParamAndRefresh', '开始 loadHistory(true)')
    this.loadHistory(true)

    setTimeout(() => {
      this.log('handleBackParamAndRefresh', '延迟 600ms 再次 loadHistory(true)')
      this.loadHistory(true)
    }, 600)
  }

  private applyMeetingBackParam(param: MeetingBackParam): void {
    if (!param) {
      this.log('applyMeetingBackParam', 'param 为空，return')
      return
    }

    const meetingId: string = param.meetingId ? String(param.meetingId) : ''
    const action: string = param.action ? String(param.action) : ''
    const status: string = param.status ? String(param.status) : ''
    const online: boolean = param.online === true

    this.log('applyMeetingBackParam',
      'meetingId=' + meetingId +
        ', action=' + action +
        ', status=' + status +
        ', online=' + String(online) +
        ', historyRows.length=' + String(this.historyRows.length)
    )

    if (meetingId.length === 0 || status.length === 0) {
      this.log('applyMeetingBackParam', 'meetingId 或 status 为空，return')
      return
    }

    const newRows: MeetingHistoryResponse[] = []

    for (let i = 0; i < this.historyRows.length; i++) {
      const oldItem: MeetingHistoryResponse = this.historyRows[i]

      if (oldItem.meetingId === meetingId) {
        const newItem: MeetingHistoryResponse = {
          meetingId: oldItem.meetingId,
          meetingNo: oldItem.meetingNo,
          roomId: oldItem.roomId,
          title: oldItem.title,
          status: status,
          hostUserId: oldItem.hostUserId,
          myRole: oldItem.myRole,
          host: oldItem.host,
          online: online,
          startTime: oldItem.startTime,
          endTime: oldItem.endTime,
          joinedAt: oldItem.joinedAt,
          leftAt: oldItem.leftAt
        }

        this.logObj('applyMeetingBackParam', '命中并替换 newItem', newItem)
        newRows.push(newItem)
      } else {
        newRows.push(oldItem)
      }
    }

    this.historyRows = newRows

    if (action === 'ended') {
      this.statusText = '会议已结束，正在刷新'
    } else if (action === 'removed') {
      this.statusText = '你已被移出会议，正在刷新'
    } else if (action === 'left') {
      this.statusText = '已离开会议，正在刷新'
    }

    this.log('applyMeetingBackParam', 'statusText=' + this.statusText)
  }

  private async handleCreateMeeting(): Promise<void> {
    const title: string = this.meetingTitle.trim()

    if (title.length === 0) {
      this.showToast('请输入会议标题')
      return
    }

    this.statusText = '正在创建会议...'

    console.info('[MeetingHomePage][handleCreateMeeting] inputTitle=' + title)

    const result = await MeetingApi.createMeeting(title, '')
    console.info('[MeetingHomePage][handleCreateMeeting] result=' + JSON.stringify(result))

    if (result.code === 200 && result.data) {
      const data: CreateMeetingResponse = result.data

      this.joinMeetingNo = data.meetingNo

      if (data.title && data.title.trim().length > 0) {
        this.meetingTitle = data.title
      } else {
        this.meetingTitle = title
      }

      this.statusText = '创建成功，会议号：' + data.meetingNo
      this.showToast('创建会议成功')

      await this.handleJoinMeeting()
      return
    }

    this.statusText = '创建失败：' + result.msg
    this.showToast(result.msg)
  }

  private async handleJoinMeeting(): Promise<void> {
    if (this.joinMeetingNo.trim().length === 0) {
      this.showToast('请输入会议号')
      return
    }

    this.statusText = '正在加入会议...'

    const result = await MeetingApi.joinMeeting(this.joinMeetingNo, '', true, true)
    console.info('[MeetingHomePage][handleJoinMeeting] ' + JSON.stringify(result))

    if (result.code === 200 && result.data) {
      const data: JoinMeetingResponse = result.data

      MeetingRuntimeStore.saveMeeting(
        data.meetingId,
        data.meetingNo,
        data.signalUrl,
        data.title
      )

      HMRouterMgr.push({
        pageUrl: 'MeetingRoomPage'
      })

      return
    }

    this.statusText = '加入失败：' + result.msg
    this.showToast(result.msg)
  }

  private async handleClickHistoryMeeting(item: MeetingHistoryResponse): Promise<void> {
    if (item.meetingNo.trim().length === 0) {
      this.showToast('会议号为空')
      return
    }

    this.statusText = '正在校验会议状态...'

    const infoResult = await MeetingApi.getMeetingInfo(item.meetingNo)
    console.info('[MeetingHomePage][handleClickHistoryMeeting] info=' + JSON.stringify(infoResult))

    if (infoResult.code !== 200 || !infoResult.data) {
      this.statusText = '会议状态校验失败：' + infoResult.msg
      this.showToast(infoResult.msg)
      await this.loadHistory(true)
      return
    }

    const info: MeetingInfoResponse = infoResult.data
    const realStatus: string = info.status ? info.status.trim() : ''

    if (realStatus !== 'running') {
      this.statusText = '会议已结束'
      this.showToast('会议已结束，不能进入')
      await this.loadHistory(true)
      return
    }

    this.joinMeetingNo = info.meetingNo

    if (item.online) {
      this.statusText = '重新进入会议：' + info.meetingNo
    } else {
      this.statusText = '重新加入会议：' + info.meetingNo
    }

    await this.handleJoinMeeting()
  }

  private async loadHistory(force: boolean = false): Promise<void> {
    this.log('loadHistory',
      '开始 force=' + String(force) +
        ', isLoadingHistory=' + String(this.isLoadingHistory) +
        ', historyRows.length=' + String(this.historyRows.length)
    )

    if (this.isLoadingHistory && !force) {
      this.log('loadHistory', '正在刷新中，忽略重复请求')
      return
    }

    this.isLoadingHistory = true

    try {
      const result = await MeetingApi.getHistoryList('all', 1, 20)
      this.logObj('loadHistory', '接口返回 result', result)

      if (result.code === 200 && result.data) {
        const rows: MeetingHistoryResponse[] = []

        for (let i = 0; i < result.data.rows.length; i++) {
          const item: MeetingHistoryResponse = result.data.rows[i]

          const newItem: MeetingHistoryResponse = {
            meetingId: item.meetingId,
            meetingNo: item.meetingNo,
            roomId: item.roomId,
            title: item.title,
            status: item.status ? item.status.trim() : '',
            hostUserId: item.hostUserId,
            myRole: item.myRole,
            host: item.host,
            online: item.online,
            startTime: item.startTime,
            endTime: item.endTime,
            joinedAt: item.joinedAt,
            leftAt: item.leftAt
          }

          rows.push(newItem)
        }

        this.historyRows = this.mergeHistoryRows(rows)

        if (this.historyRows.length === 0) {
          this.statusText = '暂无会议历史'
        } else {
          this.statusText = '会议历史已刷新'
        }

        return
      }

      this.showToast(result.msg)
    } catch (error) {
      console.error('[MeetingHomePage][loadHistory] error=' + JSON.stringify(error))
      this.showToast('会议历史刷新失败')
    } finally {
      this.isLoadingHistory = false
      this.log('loadHistory', '结束 isLoadingHistory=false')
    }
  }

  private mergeHistoryRows(sourceRows: MeetingHistoryResponse[]): MeetingHistoryResponse[] {
    this.log('mergeHistoryRows', '开始 sourceRows.length=' + String(sourceRows.length))

    const resultRows: MeetingHistoryResponse[] = []

    for (let i = 0; i < sourceRows.length; i++) {
      const item: MeetingHistoryResponse = sourceRows[i]

      const cleanItem: MeetingHistoryResponse = {
        meetingId: item.meetingId,
        meetingNo: item.meetingNo,
        roomId: item.roomId,
        title: item.title && item.title.trim().length > 0 ? item.title.trim() : '未命名会议',
        status: item.status ? item.status.trim() : '',
        hostUserId: item.hostUserId,
        myRole: item.myRole,
        host: item.host,
        online: item.online,
        startTime: item.startTime,
        endTime: item.endTime,
        joinedAt: item.joinedAt,
        leftAt: item.leftAt
      }

      let existIndex: number = -1

      for (let j = 0; j < resultRows.length; j++) {
        if (resultRows[j].meetingId === cleanItem.meetingId) {
          existIndex = j
          break
        }
      }

      if (existIndex < 0) {
        resultRows.push(cleanItem)
        continue
      }

      const oldItem: MeetingHistoryResponse = resultRows[existIndex]

      const finalStatus: string =
        oldItem.status === 'ended' || cleanItem.status === 'ended'
          ? 'ended'
          : cleanItem.status

      const finalOnline: boolean =
        finalStatus === 'ended'
          ? false
          : cleanItem.online

      const mergedItem: MeetingHistoryResponse = {
        meetingId: oldItem.meetingId,
        meetingNo: cleanItem.meetingNo.length > 0 ? cleanItem.meetingNo : oldItem.meetingNo,
        roomId: cleanItem.roomId.length > 0 ? cleanItem.roomId : oldItem.roomId,
        title: cleanItem.title.length > 0 ? cleanItem.title : oldItem.title,
        status: finalStatus,
        hostUserId: cleanItem.hostUserId,
        myRole: cleanItem.myRole,
        host: cleanItem.host,
        online: finalOnline,
        startTime: cleanItem.startTime.length > 0 ? cleanItem.startTime : oldItem.startTime,
        endTime: cleanItem.endTime.length > 0 ? cleanItem.endTime : oldItem.endTime,
        joinedAt: cleanItem.joinedAt.length > 0 ? cleanItem.joinedAt : oldItem.joinedAt,
        leftAt: cleanItem.leftAt.length > 0 ? cleanItem.leftAt : oldItem.leftAt
      }

      resultRows[existIndex] = mergedItem
    }

    this.log('mergeHistoryRows', '结束 resultRows.length=' + String(resultRows.length))
    return resultRows
  }

  private showToast(message: string): void {
    promptAction.showToast({
      message: message,
      duration: 2000
    })
  }
}
