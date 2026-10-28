# language: zh-CN
Feature: 根据标签筛选问题
  作为用户
  我希望按照标签筛选问题
  以便快速找到自己感兴趣主题的问题

  Scenario: 用户选择标签，筛选出对应问题
    Given 系统里面有带【Java】标签、【C++】标签的多条问题
    When 用户选中【Java】标签
    Then 页面只展示带有Java标签的问题

  Scenario: 清空标签筛选，恢复全部问题
    Given 当前正在按【Java】标签筛选
    When 用户点击清空筛选条件
    Then 页面展示所有公开问题

  Scenario: 选择不存在的标签
    Given 系统内没有【Rust】标签对应的问题
    When 用户选中【Rust】标签
    Then 页面提示暂无相关问题
