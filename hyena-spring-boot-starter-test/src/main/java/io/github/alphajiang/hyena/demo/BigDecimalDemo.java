package io.github.alphajiang.hyena.demo;

import java.math.BigDecimal;

public class BigDecimalDemo {

    public static void main(String[] args){
        BigDecimal bd = new BigDecimal("12.1");
        long up  = bd.setScale( 0, BigDecimal.ROUND_UP ).longValue(); // 向上取整
        long down  = bd.setScale( 0, BigDecimal.ROUND_DOWN ).longValue(); // 向下取整

        System.out.println("up value : " + up + " down value:" + down);
    }
}
