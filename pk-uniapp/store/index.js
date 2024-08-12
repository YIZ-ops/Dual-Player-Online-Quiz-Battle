import Vue from 'vue'
import Vuex from 'vuex'
import WebSocketClient from '../utils/websocket'

Vue.use(Vuex)

const store = new Vuex.Store({
    state: {
        hasLogin: uni.getStorageSync('hasLogin'),
        sessionKey: uni.getStorageSync('sessionKey'),
        socket: null,
        vsData: [{ username: '', avatar: '', roomId: '' }, { username: '', avatar: '', roomId: '' }],
        question: [],
    },
    mutations: {
        login(state, userInfo) {
            state.hasLogin = uni.getStorageSync('hasLogin')
            state.sessionKey = uni.getStorageSync('sessionKey')
        },
        logout(state) {
            state.hasLogin = false
            state.sessionKey = null
        },
        setSocket(state, socket) {
            state.socket = socket
        },
        clearSocket(state) {
            state.socket = null
        },
        setVsData(state, vsData) {
            state.vsData = vsData;
        },
        setQuestion(state, question) {
            state.question = question;
        },
    },
    actions: {
        connectSocket({commit, state}, {url, username}) {
            if (state.socket && state.socket.socket && state.socket.socket.readyState === WebSocket.OPEN) return
            state.socket = new WebSocketClient(url, username)
            state.socket.connect()
        },
    },
})

export default store
