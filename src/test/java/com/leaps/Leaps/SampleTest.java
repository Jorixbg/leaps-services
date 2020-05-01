package com.leaps.Leaps;

import org.junit.Test;

public class SampleTest {

    @Test
    public void flutTest(){
        int[] a = {12,3,10,7,16,5};
        testFlut(a);
    }

    private void testFlut(int[] prices) {
        int flutsToBuy = 0;
        int maxProfit = 0;
        int value = 0;
        for (int i = 0; i < prices.length; i++) {
            int k = i+1;
            int flutPrice = prices[i];
            value+=flutPrice;
            int profit = (10*k) - value;
            if (profit > maxProfit) {
                maxProfit = profit;
                flutsToBuy = k;
            }
        }
        System.out.println("Max profit is: " + maxProfit);
        System.out.println("Number of flutes to buy: " + flutsToBuy);
    }
}
