package com.itheima.controller;

import com.itheima.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class commonController {
    //图片路径
    @Value("${reggie.filename}")
    private String path;

    /**
     * 图片文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // file是一个临时文件，需要转存到指定位置，否则本次请求完成后会删除
        String filename = file.getOriginalFilename();
        String substring = filename.substring(filename.lastIndexOf("."));

        // 使用uuid重新生成文件名，防止文件名称重复造成文件覆盖
        String s = UUID.randomUUID() + substring;

        // 创建一个目录对象
        File f = new File(path);
        while (!f.exists()) {
            f.mkdir();
        }
        try {
            file.transferTo(new File(path +s));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(s);
    }

    /**
     * 图片文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try (
                // 输入流，通过输入流读取文件内容不够
                FileInputStream fileInputStream = new FileInputStream(new File(path) +"\\"+ name);
                // 输出流，通过输出流将图片文件协会浏览器，可以在浏览器看到图片
                ServletOutputStream outputStream = response.getOutputStream();
        ) {
            response.setContentType("image/jpeg");
            int len;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
