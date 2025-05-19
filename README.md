
# 按压动效：
## 效果视频
[点击观看视频](resource/press_animator.mp4)
<video src="resource/press_animator.mp4" controls width="600"></video>
一，滑动时被触摸的view没有动画效果，按压式才有动画

调用方式

            new PressAnimator.Builder(PressType.TYPE_DELAY).build()
                        .with(img.getContext())
                        .addTargetAnimatorView(.with(img.getContext())img,true)// addTargetAnimatorView需要添加动画的view，isMaxWh可以不传，传了所有动画view中最大宽高的view，动画是不需要额外排序，性能会更好一些
                        .addTargetAnimatorView(titleTv)
                        .addTargetAnimatorView(bottomTv)
                        .setOnTouchListener(img)// img 需要监听触摸的view
                        .init();
                      
如果业务层需要消费触摸事件，则调用方式改成如下：
           
                
                PressAnimator pressAnimator = new PressAnimator.Builder(PressType.TYPE_DELAY).build()
                .with(img.getContext())
                        .addTargetAnimatorView(img)
                        .addTargetAnimatorView(titleTv)
                        .addTargetAnimatorView(bottomTv)
                        .init();
                img.setOnTouchListener((v, event) -> {
                    pressAnimator.getOnTouchListener().onTouch(v,event);
                    return false;
                });

二，触摸控件就有按压效果
方式调用

          new PressAnimator.Builder().build()
          .with(img.getContext())
                        .addTargetAnimatorView(img,true)// addTargetAnimatorView需要添加动画的view，isMaxWh可以不传，传了所有动画view中最大宽高的view，动画是不需要额外排序，性能会更好一些
                        .addTargetAnimatorView(titleTv)
                        .addTargetAnimatorView(bottomTv)
                        .setOnTouchListener(img)// img 需要监听触摸的view
                        .init();
                        
如果业务层需要消费触摸事件，则可以通过下边方式调用

              PressAnimator pressAnimator = new PressAnimator.Builder().build()
              .with(img.getContext())
                        .addTargetAnimatorView(img)
                        .addTargetAnimatorView(titleTv)
                        .addTargetAnimatorView(bottomTv)
                        .init();
                img.setOnTouchListener((v, event) -> {
                    pressAnimator.getOnTouchListener().onTouch(v,event);
                    return false;
                });
