package com.zsb.zsbserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件存储：把前端上传的图片存到 ./uploads/{module}/yyyy/MM/ 下，
 * 返回可直接访问的 URL（/uploads/post/2026/11/xxx.png）。
 *
 * 为什么要自己校验？
 *   - 不能信前端给的扩展名（改成 .png 的可执行文件很常见）
 *   - 不能信原始文件名（可能含 ../ 路径穿越）
 *   - 必须限制大小（application.yml 已限 5MB，这里再兜一层）
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024L;

    @Value("${zsb.upload-dir:./uploads}")
    private String uploadDir;

    public String saveImage(MultipartFile file, String module) {
        if (file == null || file.isEmpty()) {
            return "";
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("图片不能超过 5MB");
        }

        String original = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        int dot = original.lastIndexOf('.');
        String ext = dot >= 0 ? original.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("只支持 jpg / png / gif / webp 格式的图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("文件类型不正确，请上传图片");
        }

        // module 会拼进磁盘路径，必须白名单化，否则传 ../../ 就能写到项目外
        String safeModule = (module != null && module.matches("[a-z]{2,12}")) ? module : "common";

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Path baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path dir = baseDir.resolve(safeModule).resolve(datePath).normalize();
        if (!dir.startsWith(baseDir)) {
            throw new IllegalArgumentException("非法的上传目录");
        }
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("创建上传目录失败", e);
        }

        // 用 UUID 重命名，彻底避开中文名/路径穿越/重名覆盖
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = dir.resolve(filename);
        try {
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("保存图片失败", e);
        }
        return "/uploads/" + safeModule + "/" + datePath + "/" + filename;
    }
}
