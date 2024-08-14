package io.yyy.handle;

import com.mysql.cj.util.StringUtils;
import io.yyy.constant.Room;
import io.yyy.entity.PkRoom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Slf4j
@Component
public class RoomCheckJob {
    @Scheduled(cron = "0 0/1 * * * ?")     // 一分钟执行一次
    public void run() {
        log.info("Checking room status: {}", WebSessionManager.roomMap);
        // 当房间列表存在10个以上时，开启空闲房间清除
        if (WebSessionManager.roomMap.size() >= Room.ROOM_NUM_CLEAR) {
            Iterator<Map.Entry<String, PkRoom>> iterator = WebSessionManager.roomMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, PkRoom> entry = iterator.next();
                PkRoom room = entry.getValue();
                if (StringUtils.isNullOrEmpty(room.getSessionAUsername()) && StringUtils.isNullOrEmpty(room.getSessionBUsername())) {
                    log.info("Removing idle room: {}", entry.getKey());
                    iterator.remove();
                }
            }
        }
    }
}
