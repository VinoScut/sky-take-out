package com.sky.service.admin;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

public interface WorkspaceService {

    BusinessDataVO todayBusinessData();

    SetmealOverViewVO setmealOverview();

    DishOverViewVO dishOverview();

    OrderOverViewVO orderOverview();
}
