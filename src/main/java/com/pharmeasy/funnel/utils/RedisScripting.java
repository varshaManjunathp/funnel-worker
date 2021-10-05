package com.pharmeasy.funnel.utils;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
@AllArgsConstructor
public class RedisScripting {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    HashMap<String, String> luaScripts;

    @Value("${env}")
    private String environment;

    RedisScripting() {
        this.luaScripts = new HashMap<>();
    }
    @PostConstruct
    private void loadScripts() throws IOException {
        File folder = new File("src/main/resources/lua");
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".lua")) {
                String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                luaScripts.put(FilenameUtils.removeExtension(file.getName()).toUpperCase(Locale.ROOT), content);
            }
        }
    }

    public Object execute(String script, Class classType, String key, Object... args) {
        RedisScript redisScript = RedisScript.of(this.luaScripts.get(script), classType);
         return redisTemplate.execute(redisScript, Collections.singletonList(key), args);
    }

    public Object getSegmentType(String segmentId) {
        String script = "TYPE";
        return ObjectUtils.nullSafeToString(execute(script, Boolean.class, getSetKeyName(segmentId)));
    }

    public String bitsetRemove(String segmentId, List<String> entityIds) {
        String script = "REM";
        return ObjectUtils.nullSafeToString(execute(script, Long.class, getTempKeyName(segmentId), entityIds.toArray()));
    }

    public String bitsetAdd(String segmentId, List<String> entityIds) {
        String script = "BITSETADD";
        return ObjectUtils.nullSafeToString(execute(script, Long.class, getTempKeyName(segmentId), entityIds.toArray()));
    }

    public String rename(String segmentId) {
        String script = "RENAME";
        return ObjectUtils.nullSafeToString(execute(script, Long.class, getTempKeyName(segmentId), getSetKeyName(segmentId)));
    }

    public String setAdd(String segmentId, List<String> entityIds) {
        BoundSetOperations<Object, Object> setOperations = redisTemplate.boundSetOps(getSetKeyName(segmentId));
        return ObjectUtils.nullSafeToString(setOperations.add(entityIds));
    }

    public String getSetKeyName(String segmentId) {
        return String.format("%s:funnel:segments:%s:set",environment,segmentId);
    }

    private String getTempKeyName(String segmentId) {
        return String.format("%s:funnel:segments:%s:new",environment,segmentId);
    }
}
