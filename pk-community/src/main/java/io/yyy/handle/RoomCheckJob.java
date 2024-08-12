package io.yyy.handle;

import com.mysql.cj.util.StringUtils;
import io.yyy.constant.RobotName;
import io.yyy.entity.PkRoom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;

@Slf4j
@Component
public class RoomCheckJob {
    @Scheduled(cron = "0 0/1 * * * ?")     // 一分钟执行一次
    public void run() {
        log.info("检查房间情况{}", WebSessionManager.rooms);
        Iterator<PkRoom> iterator = WebSessionManager.rooms.iterator();
        // 当房间列表存在10个以上时，开启空闲房间清除
        while (iterator.hasNext() && WebSessionManager.rooms.size() >= RobotName.ROOM_NUM_CLEAR) {
            PkRoom next = iterator.next();
            if (StringUtils.isNullOrEmpty(next.getSessionAUsername()) && StringUtils.isNullOrEmpty(next.getSessionBUsername())) {
                log.info("清除空闲房间");
                iterator.remove();
            }
        }
    }
}
