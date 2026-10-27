# language: zh-CN
Feature: 收藏问题
  作为登录用户
  我希望收藏感兴趣的问题
  以便之后快速找到它

  Scenario: 登录用户成功收藏问题
    Given 用户已经登录系统
    And 存在id=201的公开问题
    When 用户点击该问题的收藏按钮
    Then 该问题加入我的收藏列表
    And 按钮状态变为已收藏

  Scenario: 用户取消收藏
    Given 用户已经登录系统
    And 用户已经收藏id=201这个问题
    When 用户再次点击收藏按钮
    Then 该问题从我的收藏中移除
    And 按钮恢复为未收藏状态

  Scenario: 未登录用户尝试收藏
    Given 用户未登录
    And 存在id=201的公开问题
    When 用户点击收藏按钮
    Then 提示用户先登录
    And 收藏状态不改变
