function pickJsonObj(value) {
    try {
        return JSON.parse(value)
    } catch (e) {
        return null
    }
}

class WebSocketClient {
    constructor(url, username) {
        this.url = url
        this.socket = null
        this.username = username
        this.heartBeatDuration = 60000 // 心跳间隔，单位毫秒
        this.pingTimerDuration = 5000 // 超过这个时间，后端没有返回 pong，则判定后端断线了
        this.heartBeatTimer = null
        this.pingTimer = null
        this.destroy = false // 是否销毁
        this.eventListeners = {} // 用于存储事件监听器
    }

    connect() {
        this.socket = uni.connectSocket({
            url: this.url,
            complete: () => {
            },
        })
        this.initEventListeners()
    }

    initEventListeners() {
        this.socket.onOpen(() => {
            // WebSocket连接已打开
            this.onConnected()
            this.startHeartBeat()
        })

        this.socket.onMessage((res) => {
            const obj = pickJsonObj(res.data)
            if (this.pingTimer) {
                clearTimeout(this.pingTimer)
            }
            if (obj.type === 'pong') {
                // 收到 pong 消息，心跳正常，无需处理
                this.onMessage(res.data)
            } else {
                // 处理其他消息
                this.onMessage(res.data)
            }
        })

        this.socket.onClose((res) => {
            // WebSocket 连接已关闭
            if (this.destroy) {
                this.onClosed()
                return
            }
            this.stopHeartBeat()
        })
    }

    sendMessage(message) {
        if (this.socket) {
            this.socket.send({
                data: message,
            })
        }
    }

    startHeartBeat() {
        if (this.heartBeatTimer) {
            clearInterval(this.heartBeatTimer)
        }
        this.heartBeatTimer = setInterval(() => {
            console.log('ping')
            // 发送 ping 消息
            this.sendMessage(JSON.stringify({
                type: 'ping',
                username: this.username,
            }))
            // 未收到 pong 消息，尝试重连...
            this.pingTimer = setTimeout(() => {
                this.reconnect()
            }, this.pingTimerDuration)
        }, this.heartBeatDuration)
    }

    stopHeartBeat() {
        if (this.heartBeatTimer) {
            clearInterval(this.heartBeatTimer)
        }
        if (this.pingTimer) {
            clearTimeout(this.pingTimer)
        }
    }

    reconnect() {
        this.onReconnect()
        this.connect()
    }

    close() {
        this.destroy = true
        this.stopHeartBeat()
        if (this.socket) {
            this.socket.close()
            this.socket = null
        }
    }

    /**
     * 注册事件监听器
     * @param {string} eventType - 事件类型
     * @param {function} listener - 监听器函数
     */
    on(eventType, listener) {
        if (!this.eventListeners[eventType]) {
            this.eventListeners[eventType] = []
        }
        this.eventListeners[eventType].push(listener)
    }

    /**
     * 触发事件监听器
     * @param {string} eventType - 事件类型
     * @param {*} data - 传递给监听器的数据
     */
    trigger(eventType, data) {
        const listeners = this.eventListeners[eventType]
        if (listeners) {
            listeners.forEach(listener => listener(data))
        }
    }


    /**
     * 处理收到的消息
     * @param message
     */
    onMessage(message) {
        const obj = JSON.parse(message)
        console.log('message:', obj)
        if (obj && obj.type) {
            this.trigger(obj.type, message)
        }
    }

    /**
     * 重连时触发
     */
    onReconnect() {
        console.log('尝试重连...')
    }

    /**
     * 连接成功时触发
     */
    onConnected() {
        console.log('WebSocket 连接已打开')
    }

    /**
     * 断开时触发
     */
    onClosed() {
        console.log('WebSocket 连接已断开')
    }
}

export default WebSocketClient
