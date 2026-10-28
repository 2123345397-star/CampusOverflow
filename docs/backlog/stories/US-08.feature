# language: zh-CN
Feature: 采纳回答
  作为提问者
  我希望可以采纳满意的回答
  以便标记这个问题已经得到解决

  Scenario: 提问者成功采纳一条回答
    Given 我是id=301问题的提问人
    And 该问题下面存在一条回答
    When 我点击这条回答的【采纳】按钮
    Then 这条回答标记为已采纳
    And 问题状态变更为已解决

  Scenario: 已经采纳回答后，切换采纳别的回答
    Given 我是id=301问题提问人
    And 当前已经采纳了回答A
    When 我点击回答B的采纳按钮
    Then 取消A的采纳标记
    And 将回答B标记为已采纳

  Scenario: 非提问用户不能采纳回答
    Given 我不是id=301这个问题的提问者
    When 我点击任意回答的采纳按钮
    Then 页面提示没有权限
    And 采纳状态不变
