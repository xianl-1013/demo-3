class ContactItem {
  userId: string
  name: string
  role: string
  status: string
  avatarText: string
  color: string

  constructor(userId: string, name: string, role: string, status: string, avatarText: string, color: string) {
    this.userId = userId
    this.name = name
    this.role = role
    this.status = status
    this.avatarText = avatarText
    this.color = color
  }
}

@Component
export struct ContactPage {
  @State keyword: string = ''

  onCreateGroupMeeting: () => void = () => {}
  onInviteMember: () => void = () => {}
  onOpenOrganization: () => void = () => {}
  onStartContactMeeting: (userId: string) => void = (userId: string) => {}

  private contactList: ContactItem[] = [
    new ContactItem('1', '管理员', '系统管理员', '在线', '管', '#2F68FF'),
    new ContactItem('2', '测试成员', '会议成员', '在线', '测', '#12B76A'),
    new ContactItem('3', '项目负责人', '项目会议', '离线', '项', '#F59E0B'),
    new ContactItem('4', '运维值班', '技术支持', '在线', '运', '#7C3AED')
  ]

  build() {
    Stack() {
      Image($r('app.media.glass_bg_contact'))
        .width('100%')
        .height('100%')
        .objectFit(ImageFit.Cover)

      Column() {
        this.buildHeader()

        Scroll() {
          Column() {
            this.buildSearchBox()
            this.buildActionCard()
            this.buildCommonContactCard()
            this.buildOrganizationCard()
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
  private buildHeader() {
    Column() {
      Row() {
        Column() {
          Text('通讯录')
            .fontSize(28)
            .fontWeight(FontWeight.Bold)
            .fontColor('#1F2937')

          Text('快速邀请成员加入会议')
            .fontSize(13)
            .fontColor('#8A93A3')
            .margin({ top: 6 })
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1)

        Text('邀请')
          .fontSize(13)
          .fontColor('#2F68FF')
          .padding({ left: 14, right: 14, top: 8, bottom: 8 })
          .backgroundColor('#EAF1FF')
          .borderRadius(18)
          .onClick(() => {
            this.onInviteMember()
          })
      }
      .width('100%')
    }
    .width('100%')
    .padding({ left: 22, right: 22, top: 62, bottom: 18 })
  }

  @Builder
  private buildSearchBox() {
    Row() {
      Text('搜')
        .fontSize(15)
        .fontColor('#8A93A3')
        .margin({ right: 8 })

      TextInput({ placeholder: '搜索联系人、部门、会议成员', text: this.keyword })
        .layoutWeight(1)
        .height(48)
        .fontSize(15)
        .fontColor('#222222')
        .placeholderColor('#A0A7B5')
        .backgroundColor(Color.Transparent)
        .padding({ left: 0, right: 0 })
        .onChange((value: string) => {
          this.keyword = value
        })
    }
    .width('100%')
    .height(52)
    .padding({ left: 16, right: 16 })
    .borderRadius(26)
    .backgroundColor('#E6FFFFFF')
    .shadow({
      radius: 16,
      color: '#10000000',
      offsetX: 0,
      offsetY: 8
    })
  }

  @Builder
  private buildActionCard() {
    Row() {
      this.buildActionItem('群', '发起群会', '多人会议', () => {
        this.onCreateGroupMeeting()
      })

      this.buildActionItem('邀', '邀请成员', '发送邀请', () => {
        this.onInviteMember()
      })

      this.buildActionItem('组', '组织架构', '部门成员', () => {
        this.onOpenOrganization()
      })
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
  private buildActionItem(iconText: string, title: string, desc: string, action: () => void) {
    Column() {
      Column() {
        Text(iconText)
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#FFFFFF')
      }
      .width(50)
      .height(50)
      .borderRadius(18)
      .backgroundColor('#2F68FF')
      .justifyContent(FlexAlign.Center)
      .alignItems(HorizontalAlign.Center)

      Text(title)
        .fontSize(13)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1F2937')
        .margin({ top: 8 })

      Text(desc)
        .fontSize(11)
        .fontColor('#9CA3AF')
        .margin({ top: 3 })
    }
    .layoutWeight(1)
    .alignItems(HorizontalAlign.Center)
    .onClick(() => {
      action()
    })
  }

  @Builder
  private buildCommonContactCard() {
    Column() {
      Row() {
        Text('常用联系人')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1F2937')
          .layoutWeight(1)

        Text('全部')
          .fontSize(13)
          .fontColor('#2F68FF')
      }
      .width('100%')
      .margin({ bottom: 12 })

      ForEach(this.contactList, (item: ContactItem) => {
        this.buildContactItem(item)
      }, (item: ContactItem) => {
        return item.userId
      })
    }
    .width('100%')
    .padding(18)
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
  private buildContactItem(item: ContactItem) {
    Row() {
      Column() {
        Text(item.avatarText)
          .fontSize(17)
          .fontWeight(FontWeight.Bold)
          .fontColor('#FFFFFF')
      }
      .width(46)
      .height(46)
      .borderRadius(16)
      .backgroundColor(item.color)
      .justifyContent(FlexAlign.Center)
      .alignItems(HorizontalAlign.Center)

      Column() {
        Text(item.name)
          .fontSize(15)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1F2937')

        Text(item.role)
          .fontSize(12)
          .fontColor('#8A93A3')
          .margin({ top: 4 })
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1)
      .margin({ left: 12 })

      Column() {
        Text(item.status)
          .fontSize(12)
          .fontColor(item.status === '在线' ? '#0EA05A' : '#8A93A3')
          .padding({ left: 8, right: 8, top: 4, bottom: 4 })
          .backgroundColor(item.status === '在线' ? '#E8F8F0' : '#EEF1F6')
          .borderRadius(12)

        Text('会议')
          .fontSize(12)
          .fontColor('#2F68FF')
          .margin({ top: 7 })
          .onClick(() => {
            this.onStartContactMeeting(item.userId)
          })
      }
      .alignItems(HorizontalAlign.End)
    }
    .width('100%')
    .height(68)
  }

  @Builder
  private buildOrganizationCard() {
    Column() {
      Text('组织分组')
        .fontSize(20)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1F2937')
        .width('100%')
        .margin({ bottom: 12 })

      this.buildGroupItem('项目会议组', '4 人', '#2F68FF')
      this.buildGroupItem('技术支持组', '8 人', '#12B76A')
      this.buildGroupItem('管理协同组', '3 人', '#F59E0B')
    }
    .width('100%')
    .padding(18)
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
  private buildGroupItem(title: string, count: string, color: string) {
    Row() {
      Column()
        .width(10)
        .height(10)
        .borderRadius(5)
        .backgroundColor(color)
        .margin({ right: 12 })

      Text(title)
        .fontSize(15)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1F2937')
        .layoutWeight(1)

      Text(count)
        .fontSize(13)
        .fontColor('#8A93A3')
    }
    .width('100%')
    .height(44)
  }
}
