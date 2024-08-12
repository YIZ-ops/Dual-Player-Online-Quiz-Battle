<template>
  <view class="container" :style="{ minHeight: getHeight + 'px' }">
    <cu-custom bgColor="bg-gradual-blue" :isBack="true">
      <block slot="backText">返回</block>
      <block slot="content">对战 pk</block>
    </cu-custom>
    <view class="bannerBox">
      <!--      <image class="ggBox" mode="widthFix" src="http://cdn.zhoukaiwen.com/Banner3.jpg"></image>-->
    </view>
    <!-- 对战信息 -->
    <view class="padding board">
      <view class="score-board">
        <view class="score-avatar">
          <u-avatar :src="selfClient.avatar" class="avatar"/>
        </view>
        <view class="score-name">{{ selfClient.username }}</view>
        <view class="score-bar">
          <view :style="currentScoreBar" class="score-fill self"></view>
        </view>
        <view class="score-award">
          <view class="score-num">{{ currentScore }}</view>
        </view>
      </view>
      <view class="score-board">
        <view class="score-award">
          <span class="score-num">{{ oppositeScore }}</span>
        </view>
        <view class="score-bar">
          <view :style="oppositeScoreBar" class="score-fill opposite"></view>
        </view>
        <view class="score-avatar">
          <u-avatar :src="oppositeClient.avatar" class="avatar"/>
        </view>
        <view class="score-name">{{ oppositeClient.username }}</view>
      </view>
    </view>
    <!-- 答题主页面 -->
    <view class="padding">
      <view class="mainBox radius shadow-warp bg-white margin-top relative">
        <!--        <view class="userHeard cu-avatar lg round" style="background-image:url(http://cdn.zhoukaiwen.com/logo.png);"></view>-->
        <view v-if="hasData">
          <view class="timer" :style="timerStyle">{{ timerSec }}</view>
          <view class="animation-reverse shadow" :class="animation" data-class="slide-rights">
            <view class="mentalTitle text-xl text-black text-bold">
              <text>{{ questionArr[currentQ].id || '' }}</text>
              <text>、</text>
              <text>{{ questionArr[currentQ].question || '' }}</text>
            </view>
            <view class="mentalList"
                  v-for="(item, index) in questionArr[currentQ].itemList"
                  v-bind:class="{ chooseOptionActive: index === chooseOptionActive,
                  rightOptionActive: index === rightOptionActive }"
                  @tap="confirmAnswer(index)">
              <view class="indexBox text-shadow">{{ item.number || '' }}</view>
              {{ item.answer || '' }}
            </view>
          </view>
          <u-line color="#dddddd"></u-line>
          <view class="margin-top cu-progress radius striped active">
            <view class="bg-blue" :style="[{ width: loading ? progress + '%' : '' }]">{{ progress }}%</view>
          </view>
        </view>
        <!-- 遮罩层 -->
        <u-mask :show="maskShow">
          <view class="warp">
            <view class="rect" @tap.stop>
              <view class="closeBtn">
                <u-icon @click="maskShow=false" name="close-circle-fill" color="#ccc" size="58"></u-icon>
              </view>
              <view class="closeImg">
                <image mode="aspectFit" src="http://cdn.zhoukaiwen.com/answerTrophy.png"></image>
              </view>
              <view class="text-black text-bold text-center">恭喜您！完成答题！</view>
            </view>
          </view>
        </u-mask>
        <view v-if="resPanelShow">
          <view class="text-center text-bold text-black text-xxl margin-tb-xl">
            <view class="res-over">
              <u-avatar class="res-award" src="/static/svg/award.svg"/>
              <view class="res-winner">{{ isWinner }}</view>
            </view>
            <view class="res-head">
              <view>玩家</view>
              <view>/</view>
              <view>得分</view>
              <view>/</view>
              <view>评价</view>
            </view>
            <view class="res-item">
              <view class="item-l">
                <u-avatar class="item-avatar" :src="selfClient.avatar"/>
                <view class="item-nickname">{{ selfClient.username }}</view>
              </view>
              <view class="item-m">{{ currentScore }}"</view>
              <view class="item-r">{{ currentScore >= oppositeScore ? 'S' : 'A' }}</view>
            </view>
            <view class="res-item">
              <view class="item-l">
                <u-avatar class="item-avatar" :src="oppositeClient.avatar"/>
                <view class="item-nickname">{{ oppositeClient.username }}</view>
              </view>
              <view class="item-m">{{ oppositeScore }}"</view>
              <view class="item-r">{{ oppositeScore >= currentScore ? 'S' : 'A' }}</view>
            </view>
            <button class="continue-btn background-2" hover-class="handle-class" @click="continueQa">继续挑战</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'pk',
  data() {
    return {
      getHeight: '',
      loading: false,
      animation: '',
      hasData: false,
      progress: 0, // 进度条
      selfClient: {},
      oppositeClient: {},
      currentScore: 0, // 当前积分
      oppositeScore: 0, // 对手积分
      chooseOptionActive: -1,
      rightOptionActive: -1,
      questionArr: [],
      currentQ: 0, // 第几题
      currentA: -1, // 当前答案
      timer: null,
      timerSec: 10,
      resPanelShow: false,
      maskShow: false,
      opponentFinished: false,   // 对方是否已经完成
      selfFinished: false,       // 自己是否已经完成
      bgcolors: [
        '#178db8', '#5b89da', '#afff42', '#87ff42', '#5fff42',
        '#ff6f42', '#ff9242', '#ffb542', '#ffd742', '#ff4e42', '#37ff42',
      ],
    }
  },
  onLoad() {
    // 获取匹配信息
    this.selfClient = this.vsData[0]
    this.oppositeClient = this.vsData[1]
    // 获取问题
    const answerOptions = ['A', 'B', 'C', 'D']
    const question = this.question
    if (Array.isArray(question)) {
      this.questionArr = question.map(item => ({
        id: item.id,
        question: item.question,
        itemList: item.answer.split(',').map(ele => ({
          number: ele.split('.')[0],
          answer: ele.split('.')[1],
        })),
        answer: answerOptions.indexOf(item.trueAnswer),
        score: item.score,
      }))
      this.hasData = true
      this.initRoundModal()
    } else {
      this.questionArr = []
    }
    // 监听成绩
    this.socket.on('score', res => {
      const data = JSON.parse(res)
      this.oppositeScore += Number(data.data)
    })
    // 监听对方完成答题
    this.socket.on('opponent_finished', (res) => {
      this.opponentFinished = true
      if (this.selfFinished) {
        // 自己也已经完成，立即显示结果
        uni.hideLoading()
        this.showResultPanel()
      }
    })
    const that = this
    uni.getSystemInfo({
      success: function (res) {
        that.getHeight = res.windowHeight
      },
    })
    setTimeout(function () {
      that.loading = true
    }, 300)
  },
  onUnload() {
    if (this.timer) {
      clearInterval(this.timer)
    }
    this.initParams()
    this.currentQ = 0
    this.currentScore = 0
    this.oppositeScore = 0
    this.resPanelShow = false
    this.maskShow = false
    this.leaveRoom()
  },
  watch: {},
  computed: {
    socket() {
      return this.$store.state.socket
    },
    vsData() {
      return this.$store.state.vsData
    },
    question() {
      return this.$store.state.question
    },
    timerStyle() {
      return {
        background: this.bgcolors[10 - this.timerSec],
        transition: 'background 0.5s ease, transform 0.3s ease',
        transform: `scale(${1 + (10 - this.timerSec) * 0.02})`,
      }
    },
    currentScoreBar() {
      return `width: ${this.currentScore / 5 * 100}%;`
    },
    oppositeScoreBar() {
      return `width: ${this.oppositeScore / 5 * 100}%;`
    },
    isWinner() {
      return this.currentScore >= this.oppositeScore ? 'WINNER' : 'FAILURE'
    },
  },
  methods: {
    initParams() {
      this.chooseOptionActive = -1
      this.rightOptionActive = -1
      this.currentA = -1
      this.timerSec = 10
    },
    // 初始化回合
    initRoundModal() {
      this.initParams()
      this.runStart()
    },
    // 开启倒计时
    runStart() {
      this.timer = setInterval(() => {
        this.timerSec--
        if (this.timerSec <= 0) {
          this.runEnd()
          if (this.timer) {
            clearInterval(this.timer)
          }
        }
      }, 1000)
    },
    // 计时结束
    runEnd() {
      // 计时结束未作答
      if (this.currentA === -1) {
        this.currentA = Number(this.questionArr[this.currentQ].answer) // 避免最后一题未作答时重复显示未作答
        this.rightOptionActive = Number(this.questionArr[this.currentQ].answer)
        uni.showToast({
          title: '很遗憾未作答',
          icon: 'none',
        })
      }
      // 计时结束是最后一题
      if (this.currentQ === (this.questionArr.length - 1)) {
        this.handleLastQuestion()
        return
      }
      this.currentQ++
      this.initRoundModal()
    },
    getQuestions() {
      const answerOptions = ['A', 'B', 'C', 'D']
      this.$H.get('question/list').then(res => {
        this.questionArr = res.map(item => ({
          id: item.id,
          question: item.question,
          itemList: item.answer.split(',').map(ele => ({
            number: ele.split('.')[0],
            answer: ele.split('.')[1],
          })),
          answer: answerOptions.indexOf(item.trueAnswer),
          score: item.score,
        }))
        this.hasData = true
      })
    },
    // 确认答案
    confirmAnswer(index) {
      this.chooseOptionActive = index
      this.progress = (100 / this.questionArr.length) * (this.currentQ + 1) // 进度条
      if (this.progress < 100) {
        this.currentA = index
        const rightAnswer = Number(this.questionArr[this.currentQ].answer)
        // 回答正确
        if (this.currentA === rightAnswer) {
          const score = this.questionArr[this.currentQ].score
          this.currentScore += score
          this.updateScore(score)
        } else {
          uni.showToast({
            title: '答错了',
            icon: 'none',
          })
          this.rightOptionActive = rightAnswer
        }
        setTimeout(() => {
          this.animation = 'animation-slide-left'
          this.initParams()
          this.currentQ++ // 翻第二页
          setTimeout(() => {
            this.animation = ''
          }, 800)
        }, 300)
      } else {
        this.handleLastQuestion()
      }
    },
    // 最后一题
    handleLastQuestion() {
      if (this.timer) {
        clearInterval(this.timer)
      }
      this.selfFinished = true
      this.postRecord()  // 提交本方答题记录
      // 通知服务器本方已完成答题
      this.socket.sendMessage(
          JSON.stringify({
            type: 'self_finished',
            username: this.selfClient.username,
            data: this.currentScore,
            roomNo: this.selfClient.roomId,
          }),
      )
      this.maskShow = true
      setTimeout(() => {
        this.maskShow = false
      }, 3000)
      if (this.opponentFinished) {
        // 对方已经完成，立即显示结果
        this.showResultPanel()
      } else {
        uni.showLoading({
          title: '等待对方完成答题...',
        })
      }
      this.hasData = false
    },
    // 发送成绩
    updateScore(score) {
      this.socket.sendMessage(
          JSON.stringify({
            data: score,
            type: 'score',
            username: this.selfClient.username,
            roomNo: this.selfClient.roomId,
          }),
      )
    },
    showResultPanel() {
      this.resPanelShow = true
      this.opponentFinished = false
      this.leaveRoom()
    },
    // 继续挑战 跳转答题页面
    continueQa() {
      uni.redirectTo({
        url: '/pages/test/pkMode',
      })
    },
    // 记录成绩
    postRecord() {
      console.log('res')
    },
    // 离开房间
    leaveRoom() {
      this.socket.sendMessage(
          JSON.stringify({
            type: 'leave',
            username: this.selfClient.username,
          }),
      )
    },
  },
}
</script>

<style scoped lang="scss">
.board {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin: 0 18px;
}

.score-board {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  margin: 0 10px;
}

.score-avatar .avatar {
  width: 60px;
  height: 60px;
  border: 2px solid #4b7ed9;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  margin-bottom: 10px;
}

.score-bar {
  width: 100%;
  height: 15px;
  background-color: #ddd;
  border-radius: 10px;
  margin: 10px 0;
  position: relative;
  overflow: hidden;
}

.score-fill {
  height: 100%;
  border-radius: 10px;
}

.score-fill.self {
  background: linear-gradient(to right, #4b7ed9, #6a9ff9);
}

.score-fill.opposite {
  background: linear-gradient(to right, #d94b4b, #f96a6a);
  float: right;
}

.score-award {
  display: flex;
  align-items: center;
  margin-top: 5px;
}

.score-num {
  font-size: 20px;
  color: #333;
  font-weight: bold;
}

.score-name {
  margin-top: 10px;
  font-size: 16px;
  color: #666;
  text-align: center;
  font-weight: bold;
}

.timer {
  color: #fff;
  font-size: 22px;
  font-weight: bold;
  text-align: center;
  border-radius: 50%;
  width: 45px;
  height: 45px;
  line-height: 45px;
  margin: 10px auto
}

.warp {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.rect {
  width: 480rpx;
  // height: 520rpx;
  border-radius: 20rpx;
  padding: 25rpx 25rpx 80rpx 25rpx;
  box-sizing: border-box;
  background-color: #fff;
}

.closeImg {
  width: 100%;
  height: 200rpx;
  margin: 10rpx auto 30rpx;

  image {
    height: 200rpx;
  }
}

.cu-bar .cu-avatar:first-child {
  margin-left: -11px;
}

.cu-bar {
  height: 90rpx !important;
}

.mainBox {
  padding: 20rpx 40rpx 20rpx 40rpx;
}

.explainMain {
  width: 100%;
  text-align: center;
  font-size: 24rpx;
  color: #999999;
  margin-top: 30rpx;
}

.userHeard {
  position: absolute;
  left: 50%;
  top: -50rpx;
  margin-left: -50rpx;
}

.mentalTitle {
  margin: 0 0 30rpx 0;
}

.mentalList {
  width: 100%;
  height: 78rpx;
  line-height: 78rpx;
  padding-right: 20rpx;
  border-radius: 12rpx;
  margin: 30rpx 0;
  overflow: hidden;
  border: 1px solid #dddddd;

  .indexBox {
    width: 70rpx;
    height: 78rpx;
    text-align: center;
    line-height: 78rpx;
    float: left;
    background-color: #dddddd;
    margin-right: 15rpx;
  }
}

.chooseOptionActive {
  width: 100%;
  height: 78rpx;
  line-height: 78rpx;
  padding-right: 20rpx;
  border: 1px solid #0081ff;
  border-radius: 12rpx;
  color: #0081ff;
  margin: 30rpx 0;
  font-weight: 600;
  background-image: url(http://cdn.zhoukaiwen.com/answerYes.png);
  background-repeat: no-repeat;
  background-size: 38rpx;
  background-position: right 30rpx center;

  .indexBox {
    background-color: #0081ff;
    color: #ffffff;
  }
}

.rightOptionActive {
  width: 100%;
  height: 78rpx;
  line-height: 78rpx;
  padding-right: 20rpx;
  border: 1px solid #4aa1bc;
  border-radius: 12rpx;
  color: #4aa1bc;
  margin: 30rpx 0;
  font-weight: 600;
  background-image: url(http://cdn.zhoukaiwen.com/answerYes.png);
  background-repeat: no-repeat;
  background-size: 38rpx;
  background-position: right 30rpx center;

  .indexBox {
    background-color: #4aa1bc;
    color: #ffffff;
  }
}

.container {
  background-color: #f2f2f2;

  .bannerBox {
    width: 750rpx;

    image {
      width: 750rpx;
    }
  }

  .explain {
    width: 750rpx;
    // position: absolute;
    bottom: 40rpx;
    font-size: 24rpx;
    margin: 10rpx 0 40rpx 0;
    text-align: center;
    color: #999999;

    .integral {
      font-size: 30rpx;
      margin-bottom: 15rpx;
      color: #333333;
    }
  }
}

.res-over {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-bottom: 20px;

  .res-award {
    width: 64px;
    height: 64px;
  }

  .res-winner {
    margin-left: 20px;
    font-size: 32px;
    color: #ee5500;
  }
}

.res-head {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  color: #fff;
  padding: 10px 20px;
  border-bottom: 1rpx solid #ddd;
  background: #555;
}

.res-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  border-bottom: 1rpx solid #ccc;
  background: #f0f0f0;

  .item-l {
    width: 80px;
    flex-shrink: 0;
    position: relative;

    .item-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
    }

    .item-nickname {
      font-size: 12px;
      color: #333;
      width: 100%;
      text-overflow: ellipsis;
      overflow: hidden;
      white-space: nowrap;
    }
  }

  .item-m {
    font-size: 20px;
    color: #00cc00;
    position: relative;
    left: -8px;
  }

  .item-r {
    width: 60px;
    flex-shrink: 0;
    font-size: 24px;
    color: #ee5500;
    text-align: right;
  }
}

.continue-btn {
  border-radius: 0;
  margin: 30px 30px 0;
  color: #000000;
}
</style>
