package com.example.service;

import com.example.annotation.MeasureTaskDuration;
import org.springframework.stereotype.Service;

@Service
public class TaskAopDemoService {

  @MeasureTaskDuration
  public String measuredStep() {
    return "Task operation executed";
  }

  public String invokeInternally() {
    return this.measuredStep();
  }
}