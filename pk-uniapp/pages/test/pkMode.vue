<template>
  <view class="page">
    <button class="match-btn" @click="findMatch">匹配对手</button>
    <view v-if="matching">
      <view class="gooey">
        <view class="dot"></view>
        <view class="dots">
          <view></view>
          <view></view>
          <view></view>
        </view>
      </view>
      <view class="matching-text">matching...</view>
      <button @click="cancelMatch" class="cancel-btn">取消匹配</button>
    </view>

    <view class="vs-modal" v-if="showVSModal">
      <view class="vs-modal-content">
        <view class="player-l">
          <view class="headimg">
            <u-avatar :src="vsData[0].avatar" size="130"/>
          </view>
          <view class="username">{{ vsData[0].username }}</view>
        </view>
        <view style="font-size: 32px;color:#fff;">VS</view>
        <view class="player-r">
          <view class="headimg">
            <u-avatar :src="vsData[1].avatar" size="130"/>
          </view>
          <view class="username">{{ vsData[1].username }}</view>
        </view>
      </view>
    </view>
  </view>
</template>
<script>
export default {
  data() {
    return {
      matching: false,
      showVSModal: false,
      connected: false,
      username: '',
      avatar: '',
      matchTimeout: 30000, // 30 s
      matchTimer: null,
    }
  },
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
  },
  onLoad() {
    this.username = uni.getStorageSync('userInfo').username
    this.avatar = uni.getStorageSync('userInfo').avatar
    if (!this.socket) {
      this.$store.dispatch('connectSocket', {
        url: 'ws://127.0.0.1:8080/websocket',
        username: this.username,
      })
    }
    setTimeout(() => {
      if (!this.socket || !this.socket.socket || this.socket.socket.readyState !== WebSocket.OPEN) {
        uni.showModal({
          title: '提示',
          content: '你已经失去连接,请尝试重新进入',
          showCancel: false,
        })
      }
    }, 800)
  },
  onUnload() {
    this.cancelMatch()
  },
  methods: {
    // 匹配对手
    findMatch() {
      if (this.matching) return
      this.matching = true
      this.startMatchTimer()
      // 发起匹配
      this.socket.sendMessage(
          JSON.stringify({
            type: 'pk',
            username: this.username,
            avatar: this.avatar,
          }),
      )
      // 匹配成功
      this.socket.on('matched', this.handleMatchSuccess)
      // 获取试卷
      this.socket.on('question', this.handleQuestion)
    },
    // 开始匹配计时
    startMatchTimer() {
      this.matchTimer = setTimeout(() => {
        this.handleMatchTimeout()
      }, this.matchTimeout)
    },
    // 匹配超时
    handleMatchTimeout() {
      clearTimeout(this.matchTimer)
      this.matching = false
      this.leaveRoom()
      uni.showModal({
        title: '提示',
        content: '好像找不到对手诶',
        showCancel: false,
      })
    },
    // 匹配成功
    handleMatchSuccess(res) {
      clearTimeout(this.matchTimer)
      const data = JSON.parse(res)
      this.$store.commit('setVsData', [
        { // 己方
          username: this.username,
          avatar: this.avatar,
          roomId: data.data,
        },
        { // 对方
          username: data.username,
          avatar: data.avatar,
          roomId: data.data,
        },
      ])
      this.matching = false
      this.showVSModal = true
      // 检查问题是否已接收
      if (this.question && this.question.length > 0) {
        this.navigateToPKPage()
      }
    },
    // 获取试卷
    handleQuestion(res) {
      clearTimeout(this.matchTimer)
      const data = JSON.parse(res)
      this.$store.commit('setQuestion', data.data)
      // 检查匹配数据是否已接收
      if (this.vsData[0].username && this.vsData[1].username) {
        this.navigateToPKPage()
      }
    },
    // 跳转
    navigateToPKPage() {
      setTimeout(() => {
        this.showVSModal = false
        uni.navigateTo({
          url: '/pages/test/pk',
        })
      }, 2000)
    },
    // 取消匹配
    cancelMatch() {
      this.matching = false
      clearTimeout(this.matchTimer)
      this.leaveRoom()
    },
    // 离开房间
    leaveRoom() {
      this.socket.sendMessage(
          JSON.stringify({
            type: 'leave',
            username: this.username,
          }),
      )
    },
  },
}
</script>
<style lang="scss">
.match-btn, .cancel-btn {
  width: 300rpx;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 20rpx;
  font-size: 32rpx;
  font-weight: bold;
  transition: all 0.3s;
  border: none;
  outline: none;
}

.match-btn {
  background-color: #1E90FF;
  color: #fff;
  box-shadow: 0 10rpx 20rpx rgba(30, 144, 255, 0.3);

  &:hover {
    background-color: #4169E1;
    box-shadow: 0 15rpx 25rpx rgba(30, 144, 255, 0.4);
  }
}

.cancel-btn {
  background-color: #FF4500;
  color: #fff;
  box-shadow: 0 10rpx 20rpx rgba(255, 69, 0, 0.3);

  &:hover {
    background-color: #FF6347;
    box-shadow: 0 15rpx 25rpx rgba(255, 69, 0, 0.4);
  }
}

.matching-text {
  position: absolute;
  display: inline-block;
  width: 100%;
  height: 30px;
  left: 0;
  top: 55%;
  text-align: center;
  color: #222;
}

.gooey {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 142px;
  height: 40px;
  margin: -20px 0 0 -71px;
  background: #fff;
  filter: contrast(20);
}

.gooey .dot {
  position: absolute;
  width: 16px;
  height: 16px;
  top: 12px;
  left: 15px;
  filter: blur(4px);
  background: #000;
  border-radius: 50%;
  transform: translateX(0);
  animation: dot 2.8s infinite;
}

.gooey .dots {
  transform: translateX(0);
  margin-top: 12px;
  margin-left: 31px;
  animation: dots 2.8s infinite;
}

.gooey .dots view {
  display: block;
  float: left;
  width: 16px;
  height: 16px;
  margin-left: 16px;
  filter: blur(4px);
  background: #000;
  border-radius: 50%;
}

@keyframes dot {
  50% {
    transform: translateX(96px);
  }
}

@keyframes dots {
  50% {
    transform: translateX(-31px);
  }
}

//----------------------对战弹层----------------------*/
.vs-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.vs-modal-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 90%;
  max-width: 600px;
  height: 25%;
  padding: 20px;
  background: linear-gradient(135deg, #4b7ed9 50%, #444 50%);
  border-radius: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.6);
}

.player-l, .player-r {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  text-align: center;
}

.headimg {
  width: 80px;
  height: 80px;
  margin-bottom: 10px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.u-avatar {
  top: 8%
}

.username {
  font-size: 10px;
  color: #fff;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.vs-modal-content > view {
  font-size: 40px;
  color: #fff;
  font-weight: bold;
  margin: 0 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  animation: vs-fade 1.5s infinite alternate;
}

@keyframes vs-fade {
  from {
    transform: scale(1);
  }
  to {
    transform: scale(1.2);
  }
}
</style>

