package com.gvbrain.appApi.assessmentapp.testcaseapi;

import com.gvbrain.appApi.Utils.RandomValueUtil;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.hamcrest.Matchers.*;

class AssessmentPlanManagerTest {

    AssessmentPlanManager assessmentPlanManager;

    @BeforeAll
    static void beforeAll(){
        new AssessmentPlanManager().deleteAllAssessmentPlan();
    }

    @BeforeEach
    void setup(){
        if (assessmentPlanManager == null){
            assessmentPlanManager = new AssessmentPlanManager();
        }
    }

    /**
     * 创建自定义测评方案,获取方案uid
     * @return
     */
    Integer createPlanGetUid(){
        String assessmentPlanDescribe = RandomValueUtil.getRandomAssessmentPlanDescribe();
        String assessmentPlanName = RandomValueUtil.getRandomAssessmentPlanName();
        //获取随机量表uid列表，可能有重复，测试服务端是否做去重处理
        List<Integer> items = RandomValueUtil.getRandomAssessmentUidList();
        Integer uid = assessmentPlanManager.createAssessmentPlan(assessmentPlanDescribe,assessmentPlanName,items).then().statusCode(200)
                .body("status",equalTo("1"))
                .extract().path("body.uid");
        return uid;
    }

    /**
     * 查询当前登陆用户的自定义测评方案
     * @return
     */
    @Test
    void getAssessmentPlan() {
        assessmentPlanManager.getAssessmentPlan().then().statusCode(200).body("status", equalTo("1"));
    }

    /**
     * 创建自定义测评方案，检查是否包含
     */
    @Test
    void createAssessmentPlan() {
        //创建自定义方案
        Integer uid = createPlanGetUid();
        //查询方案是否创建成功,检查自定义方案中是否包含已创建的方案
        assessmentPlanManager.getAssessmentPlan().then().statusCode(200)
                .body("status",equalTo("1"))
                .body("body.uid",hasItem(uid));
    }

    @Test
    void deleteAssessmentPlan(){
        Integer uid = createPlanGetUid();
        //删除自定义方案
        assessmentPlanManager.deleteAssessmentPlan(uid).then().statusCode(200).body("status",equalTo("1"));
        //查询方案是否真正删除
        assessmentPlanManager.getAssessmentPlan().then().statusCode(200)
                .body("status",equalTo("1"))
                .body("body.uid",not(hasItem(uid)));
    }

    /*@Test
    void deleteAllAssessmentPlan() {
        assessmentPlanManager.deleteAllAssessmentPlan();
    }*/

    /**
     * 设置首页展示与取消并检查展示结果
     */
    @Test
    void setIsShow() {
        DoctorManager doctorManager = new DoctorManager();
        //创建自定义方案
        Integer uid = createPlanGetUid();
        //设置首页可见
        assessmentPlanManager.setIsShow(uid).then().statusCode(200).body("status",equalTo("1"));
        //检查首页是否可见此方案，isShow=0
        doctorManager.getDoctorInfo().then().statusCode(200)
                .body("status",equalTo("1"))
                .body("body.assessmentPlans.uid",hasItem(uid))
                .body("body.assessmentPlans.isShow[0]",equalTo(0));
        //设置首页不可见
        assessmentPlanManager.setIsShow(uid).then().statusCode(200).body("status",equalTo("1"));
        //检查首页是否不可见此方案
        doctorManager.getDoctorInfo().then().statusCode(200)
                .body("status",equalTo("1"))
                .body("body.assessmentPlans.uid",not(hasItem(uid)));
    }

    @Test
    void updateAssessmentPlan() {
        //创建自定义测评方案
        Integer uid = createPlanGetUid();
        //更新自定义方案信息
        String assessmentPlanDescribe = RandomValueUtil.getRandomAssessmentPlanDescribe();
        String assessmentPlanName = RandomValueUtil.getRandomAssessmentPlanName();
        List<Integer> items = RandomValueUtil.getRandomAssessmentUidList();
        //对List items去重，作为服务端处理结果校验备用
        LinkedHashSet<Integer> set = new LinkedHashSet<>(items.size());
        List<Integer> uniqueItems = new ArrayList<>(items.size());
        set.addAll(items);
        uniqueItems.addAll(set);
        /*HashSet<Integer> set = new HashSet<>(items.size());
        List<Integer> uniqueItems = new ArrayList<>(items.size());
        items.forEach(key->{
            if (set.add(key)){
                uniqueItems.add(key);
            }
        });
        items.clear();
        items.addAll(uniqueItems);*/
        //检查更新结果，对更新后的方案uid、方案描述、方案名称和量表uid进行检查
        List<Integer>  assUids = assessmentPlanManager.updateAssessmentPlan(assessmentPlanDescribe,assessmentPlanName,items,uid).then().statusCode(200)
                .body("status",equalTo("1"))
                .body("body.uid",equalTo(uid))
                .body("body.assessmentPlanDescribe",equalTo(assessmentPlanDescribe))
                .body("body.assessmentPlanName",equalTo(assessmentPlanName))
                .extract().path("body.assessmentItems.uid");
        assUids.forEach(key->{
            System.out.println("assUid======"+key);
            MatcherAssert.assertThat(key,isIn(items));
        });
    }


}