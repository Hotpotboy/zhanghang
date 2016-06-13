package com.example.soucang;

import com.example.ITest;

/**
 * Created by hangzhang209526 on 2016/6/3.
 * 题目：n个数字（0,1,…,n-1）形成一个圆圈，从数字0开始，
 每次从这个圆圈中删除第m个数字（第一个为当前数字本身，第二个为当前数字的下一个数字）。
 当一个数字删除后，从被删除数字的下一个继续删除第m个数字。
 求出在这个圆圈中剩下的最后一个数字。
 */
public class ShanchuYuanQuan implements ITest {
    private Node root;
    private int deleteM;
    public ShanchuYuanQuan(int n,int m){
        deleteM = m;
        Node node = new Node();
        Node lastNode=null;
        for(int i=n-1;i>0;i--){
            node.value = i;
            node.index = i;
            if(i==n-1) lastNode = node;
            Node node1 = new Node();
            node1.index = i-1;
            node1.value = i-1;
            node.pre=node1;
            node = node1;
            if(i==1){
                root = node1;
                root.pre = lastNode;
            }
        }
    }

    @Override
    public void test() {
        int len;
        while ((len=getQuanNum())!=1){
            Node deletedNode;//删除的节点
            Node nextNode = root;//上一个节点指向删除节点的节点
            while (true){
                Node pre = nextNode.pre;
                if(pre.index==deleteM%len){//找到删除的节点
                    deletedNode = pre;
                    break;
                }
                nextNode=pre;
            }
            deleteNode(deletedNode,nextNode);//删除节点
            setRoot(nextNode);
        }
        System.out.print("结果为:"+root.value);
    }

    private void setRoot(Node node){
        if(root!=node){
            node.index=0;
            root=node;
            int num = getQuanNum()-1;
            Node start = root;
            do{
                Node pre = start.pre;
                pre.index=num;
                num--;
                start = pre;
            }while (start!=root);
        }
    }

    private void deleteNode(Node deletedNode,Node next){
        Node tmp = deletedNode.pre;
        deletedNode.pre=null;
        next.pre = tmp;
    }

    /**
     * 获取当前圆圈的总数
     * @return
     */
    private int getQuanNum(){
        int num = 1;
        Node pre = root.pre;
        while (pre!=root){
            num++;
            pre = pre.pre;
        }
        return num;
    }

    public class Node{
        private Node pre;
        private int value;
        private int index;
    }
}
