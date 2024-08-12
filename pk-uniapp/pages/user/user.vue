<template>
  <view>
    <view class="head">
      <block v-if="hasLogin">
        <view class="userinfo" @click="goUser">
          <u-avatar :src="userInfo.avatar"></u-avatar>
          <view class="username">
            <text>{{ userInfo.username }}</text>
            <text class="sub-txt">{{ userInfo.intro }}</text>
          </view>
          <u-icon name="arrow-right" class="arrow-right"></u-icon>
        </view>
      </block>
      <block v-else>
        <view class="btn-login">
          <!-- #ifdef H5 -->
          <u-button type="default" shape="circle" @click="phoneLogin" plain>登录</u-button>
          <!-- #endif -->
          <!-- #ifdef MP-WEIXIN -->
          <u-button type="default" shape="circle" @click="wxLogin" plain>登录</u-button>
          <!-- #endif -->
        </view>
      </block>
    </view>
  </view>
</template>

<script>

export default {
  data() {
    return {
      userInfo: '',
      hasLogin: false
    }
  },
  onShow() {
    if (uni.getStorageSync('hasLogin')) {
      this.getUserInfo()
      this.hasLogin = true
    } else {
      this.hasLogin = false
    }
  },
  methods: {
    phoneLogin() {
      uni.navigateTo({
        url: '/pages/login/login',
      })
    },
    wxLogin() {
      uni.navigateTo({
        url: '/pages/login/weixin',
      })
    },
    getUserInfo() {
      this.$H.get('user/userInfo').then(res => {
        this.userInfo = res.result
      })
    },
    goUser() {
      uni.navigateTo({
        url: '/pages/user/edit',
      })
    },
  },
}
</script>

<style>
page {
  background-color: #f5f5f5;
}
</style>
<style lang="scss" scoped>
.head {
  padding: 20rpx;
  background-color: #fff;

  .sub-txt {
    font-size: 24rpx;
    color: #616161;
    display: block;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    overflow: hidden;
  }

  margin-bottom: 20rpx;
}

.userinfo {
  display: flex;
  align-items: center;
  padding: 20rpx;
}

.userinfo .username {
  display: flex;
  flex-direction: column;
  margin-left: 20rpx;
}

.nav-text {
  color: #999;
  font-size: 14px;
  margin-bottom: 20rpx;
}

.grid-text {
  color: #999;
  font-size: 12px;
  margin-bottom: 20rpx;
}

.userinfo u-avatar {
  margin-right: 20rpx;
}

.userinfo .arrow-right {
  margin-left: auto;
}

.btn-login {
  margin: 40rpx 0;
}

.gn-icon {
  width: 60rpx;
  height: 60rpx;
  margin-bottom: 20rpx;
}

/*服务按钮*/
.btn-wrap {
  display: flex;
  margin-top: 30rpx;
}

.btn-wrap .btn-contact {
  background-color: #fff;
  margin-left: 15rpx;
  margin-right: 15rpx;
  padding: 20rpx;
  line-height: unset;
  font-size: 12px;
  color: #999;
}

.btn-wrap .btn-contact:active {
  background-color: #f5f5f5;
}

.btn-wrap .btn-contact .txt {
  margin-top: 20rpx;
}

.btn-wrap .btn-contact::after {
  border: unset;
  position: unset;
}

.icon-size {
  font-size: 50rpx;
}

.block-wrap {
  background-color: #fff;
  border-radius: 20rpx;
  margin: 20rpx;
  overflow: hidden;

  .block-title {
    background-color: #fff;
    padding: 20rpx;
  }
}

.bottom-info {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #737373;
  font-size: 32rpx;
  font-weight: 600;
}

.bottom-info-t {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1f1f1f;
  font-size: 28rpx;
  font-weight: 400;
}
</style>
