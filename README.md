# 须臾图库项目

------

## 项目介绍

基于Vue 3 + Spring Boot + COS + WebSocket 的智能协同云图库平台

本项目分为三个模块：公共素材空间，用户私人空间，团队空间。

### 一、公共图库

1）所有用户都可以在平台上传和检索图片素材，快速查找到所需图片。

![界面](https://github.com/magicallyliu/test-git-project/blob/master/MomentGallery/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-10-30%20195343.png?raw=true)

2）管理员可以对用户上传的公开图片进行查看、修改、审核等一系列操作。

![管理员图片管理界面](https://github.com/magicallyliu/test-git-project/blob/master/MomentGallery/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-10-30%20203617.png?raw=true)

### 二、私有空间

1）为用户提供一个私有的空间，用户能将图片上传到私有空间进行批量管理、检索、编辑和分析，可以用作个人相册，作品集等。

![私有空间](https://github.com/magicallyliu/test-git-project/blob/master/MomentGallery/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-10-30%20204740.png?raw=true)

### 三、团队空间

1）能够让用户开通团队空间，使团队成员可以共享图片并实时编辑图片。

![团队空间](J:/Project/PlanetProject/test-git-project/!%255B%25E5%25B1%258F%25E5%25B9%2595%25E6%2588%25AA%25E5%259B%25BE%25202025-10-30%2520195603.png%255D(https:/github.com/magicallyliu/test-git-project/blob/master/MomentGallery/%25E5%25B1%258F%25E5%25B9%2595%25E6%2588%25AA%25E5%259B%25BE%25202025-10-30%2520195603.png)

2）管理员可以邀请成员进入团队空间，并为其赋予不同的角色权限。

![团队空间成员邀请](J:/Project/PlanetProject/test-git-project/!%255B%25E5%25B1%258F%25E5%25B9%2595%25E6%2588%25AA%25E5%259B%25BE%25202025-10-30%2520200331.png%255D(https:/github.com/magicallyliu/test-git-project/blob/master/MomentGallery/%25E5%25B1%258F%25E5%25B9%2595%25E6%2588%25AA%25E5%259B%25BE%25202025-10-30%2520200331.png)

#### 四、重点功能

##### 1. 图片分析

能够对不同空间进行空间分析，从而了解存储量，图片数量，分类占比等。

![图片分析](https://github.com/magicallyliu/test-git-project/blob/master/MomentGallery/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-10-30%20195431.png?raw=true)

![图片分析2](https://github.com/magicallyliu/test-git-project/blob/master/MomentGallery/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-10-30%20195431.png?raw=true)

##### 2. 图片编辑

###### 1）常规编辑

为用户提供了基础的的裁剪，旋转等。

![编辑](https://github.com/magicallyliu/test-git-project/blob/master/MomentGallery/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-10-30%20200606.png?raw=true)

###### 2）AI扩图功能

能够通过AI对图片进行扩图。

![AI扩图](https://github.com/magicallyliu/test-git-project/blob/master/MomentGallery/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-10-30%20201832.png?raw=true)

###### 3）协调编辑

为团队空间提供了协调编辑功能，能弄让团队成员同时编辑图片时，只有一位成员能够编辑，其他成员实时查看编辑效果和操作提示

![协调编辑](https://github.com/magicallyliu/test-git-project/blob/master/MomentGallery/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-10-30%20202606.png?raw=true)
