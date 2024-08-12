<template>
  <view class="container" :style="{ minHeight: getHeight + 'px' }">
    <cu-custom bgColor="bg-gradual-blue" :isBack="true">
      <block slot="backText">返回</block>
      <block slot="content">随机答题</block>
    </cu-custom>

    <view class="bannerBox">
      <!-- 可以在这里添加一个适合随机答题的banner图 -->
    </view>

    <view class="userinfo">
      <u-avatar :src="userInfo.avatar"></u-avatar>
      <view class="username">
        <text>{{ userInfo.username }}</text>
        <text class="sub-txt">{{ userInfo.intro }}</text>
      </view>
    </view>

    <!-- 答题主页面 -->
    <view class="padding">
      <view class="mainBox radius shadow-warp bg-white margin-top relative">
        <view v-show="hasData">
          <view class="animation-reverse shadow" :class="animation" data-class="slide-rights" v-if="currentQuestion">
            <view class="mentalTitle text-xl text-black text-bold">
              <text>{{ currentQuestionIndex +1}}</text>
              <text>、</text>
              <text>{{ currentQuestion.title || '' }}</text>
            </view>
            <view class="mentalList" v-for="(item, index) in currentQuestion.itemList" :key="index"
                  v-bind:class="{
                    mentalListActive: showAnswer && index === currentQuestion.correctAnswer,
                    mentalListWrong: showAnswer && index === selectedAnswer && index !== currentQuestion.correctAnswer
                  }"
                  @tap="selectAnswer(index)">
              <view class="indexBox text-shadow">{{ item.number || '' }}</view>
              {{ item.answer || '' }}
            </view>
          </view>

          <u-line color="#dddddd"></u-line>

          <view class="margin-top cu-progress radius striped active">
            <view class="bg-blue" :style="[{ width: loading ? progress + '%' : '' }]">{{ progress }}%</view>
          </view>
        </view>

        <view v-show="noData">
          <view class="text-center text-bold text-black text-xxl margin-tb-xl">答题完成，感谢参与</view>
        </view>
      </view>
    </view>

    <!-- 结果弹窗 -->
    <u-mask :show="showResult">
      <view class="warp">
        <view class="rect" @tap.stop>
          <view class="closeBtn">
            <u-icon @click="closeResult" name="close-circle-fill" color="#ccc" size="58"></u-icon>
          </view>
          <view class="content">
            <view class="closeImg">
              <image mode="aspectFit" src="http://cdn.zhoukaiwen.com/answerTrophy.png"></image>
            </view>
            <view class="text-black text-bold text-center">恭喜您！完成答题！</view>
            <view class="text-center margin-tb-lg text-gray">{{ resultMessage }}</view>
            <view class="text-center text-blue">正确率：{{ correctRate }}%</view>
          </view>
        </view>
      </view>
    </u-mask>

  </view>
</template>

<script>
export default {
  name: "randomQuiz",
  data() {
    return {
      userInfo: '',
      getHeight: '',
      loading: false,
      animation: '',
      selectedAnswer: -1,
      noData: false,
      hasData: true,
      showResult: false,
      showAnswer: false,

      // 题目数据
      questions: [{
        serialNumber: '1',
        name: '',
        title: '下列元素中，为行内元素的是（ ）',
        correctAnswer: 0,
        itemList: [{
          number: 'A',
          answer: 'div'
        },
          {
            number: 'B',
            answer: 'span'
          },
          {
            number: 'C',
            answer: 'p'
          },
          {
            number: 'D',
            answer: 'h3'
          }
        ]
      },
        {
          serialNumber: '2',
          name: '',
          title: '下列是设置边框的属性（）',
          correctAnswer: 2,
          itemList: [{
            number: 'A',
            answer: 'border-color'
          },
            {
              number: 'B',
              answer: 'box-sizing'
            },
            {
              number: 'C',
              answer: 'background'
            },
            {
              number: 'D',
              answer: 'visibility'
            }
          ]
        },
        {
          serialNumber: '3',
          name: '',
          title: '在javascript里，下列选项中不属于数组方法的是（ ）',
          correctAnswer: 1,
          itemList: [{
            number: 'A',
            answer: 'sort()'
          },
            {
              number: 'B',
              answer: 'length()'
            },
            {
              number: 'C',
              answer: 'concat()'
            },
            {
              number: 'D',
              answer: 'reverse()'
            }
          ]
        },
        {
          serialNumber: '4',
          name: '',
          title: '下列属性哪一个能够实现层的隐藏？',
          correctAnswer: 1,
          itemList: [{
            number: 'A',
            answer: 'display:fals'
          },
            {
              number: 'B',
              answer: 'display:hidden'
            },
            {
              number: 'C',
              answer: 'display:none'
            },
            {
              number: 'D',
              answer: 'display:“”'
            }
          ]
        },
        {
          serialNumber: '5',
          name: '',
          title: '以下哪个单词不属于javascript关键字',
          correctAnswer: 3,
          itemList: [{
            number: 'A',
            answer: 'with'
          },
            {
              number: 'B',
              answer: 'parent'
            },
            {
              number: 'C',
              answer: 'class'
            },
            {
              number: 'D',
              answer: 'void'
            }
          ]
        },
      ],
      currentQuestionIndex: 0,
      totalQuestions: 5, // 设置总题目数
      correctAnswers: 0,

      //进度条
      progress: 0,

      resultMessage: '',
      correctRate: 0
    };
  },
  computed: {
    currentQuestion() {
      return this.questions[this.currentQuestionIndex] || {};
    }
  },
  onLoad() {
    this.getSystemInfo();
    this.getUserInfo();
    this.shuffleQuestions();
  },
  methods: {
    getSystemInfo() {
      uni.getSystemInfo({
        success: (res) => {
          this.getHeight = res.windowHeight;
        }
      });
      setTimeout(() => {
        this.loading = true;
      }, 300);
    },
    getUserInfo() {
      this.$H.get('user/userInfo').then(res => {
        this.userInfo = res.result;
      });
    },
    shuffleQuestions() {
      // 随机打乱题目顺序，并只取指定数量的题目
      this.questions = this.shuffleArray(this.questions).slice(0, this.totalQuestions);
      this.updateProgress();
    },
    shuffleArray(array) {
      for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]];
      }
      return array;
    },
    selectAnswer(index) {
      if (this.showAnswer) return; // 防止重复选择
      this.selectedAnswer = index;
      this.showAnswer = true;

      // 检查答案是否正确
      if (index === this.currentQuestion.correctAnswer) {
        this.correctAnswers++;
      }

      setTimeout(() => {
        this.nextQuestion();
      }, 1000);
    },
    nextQuestion() {
      if (this.currentQuestionIndex < this.totalQuestions - 1) {
        this.currentQuestionIndex++;
        this.selectedAnswer = -1;
        this.showAnswer = false;
        this.animation = 'animation-slide-left';
        setTimeout(() => {
          this.animation = '';
        }, 800);
      } else {
        this.finishQuiz();
      }
      this.updateProgress();
    },
    updateProgress() {
      this.progress = ((this.currentQuestionIndex ) / this.totalQuestions) * 100;
    },
    finishQuiz() {
      this.noData = true;
      this.hasData = false;
      this.correctRate = Math.round((this.correctAnswers / this.totalQuestions) * 100);
      this.resultMessage = `您答对了 ${this.correctAnswers} 道题目，共 ${this.totalQuestions} 道题。`;
      setTimeout(() => {
        this.showResult = true;
      }, 500);
    },
    closeResult() {
      this.showResult = false;
      // 可以在这里添加重新开始答题或返回主页的逻辑
    }
  }
}
</script>

<style scoped lang="scss">

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
  margin: 20rpx auto 30rpx;
  display: flex;
  justify-content: center;
  align-items: center;

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
  padding: 60rpx 40rpx 20rpx 40rpx;
}

.explainMain {
  width: 100%;
  text-align: center;
  font-size: 24rpx;
  text-align: center;
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

.mentalListActive {
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
.mentalListWrong {
  width: 100%;
  height: 78rpx;
  line-height: 78rpx;
  padding-right: 20rpx;
  border: 1px solid #ff0000;
  border-radius: 12rpx;
  color: #ff0000;
  margin: 30rpx 0;
  font-weight: 600;
  background-image: url(http://cdn.zhoukaiwen.com/answerNo.png);
  background-repeat: no-repeat;
  background-size: 38rpx;
  background-position: right 30rpx center;

  .indexBox {
    background-color: #ff0000;
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
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  /* 根据需要调整内边距 */
  padding: 20px;
}
</style>