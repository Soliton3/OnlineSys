package com.atguigu.oss.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    //上传文件到oss
    String upLoadFileAvatar(MultipartFile file);
}
