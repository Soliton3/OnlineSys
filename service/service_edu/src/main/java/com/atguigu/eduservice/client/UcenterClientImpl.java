package com.atguigu.eduservice.client;


import com.atguigu.commonutils.ordervo.UcenterMemberOrder;
import org.springframework.stereotype.Component;

@Component
public class UcenterClientImpl implements UcenterClient{
    @Override
    public UcenterMemberOrder getUcenterPay(String memberId) {
        return null;
    }
}
