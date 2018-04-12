package com.sfh.lib.ui;

import android.support.v4.app.DialogFragment;
import android.view.Gravity;

/**
 * 功能描述:提示对话框接口
 *
 * @date 2016/11/14
 */

public interface DialogView {

    interface DialogInterface {

        void dismiss();

        interface OnClickListener {
            void onClick(DialogFragment dialog, int which);
        }
    }

    final class Builder {
        CharSequence title;
        CharSequence message;
        DialogInterface.OnClickListener leftListener;
        DialogInterface.OnClickListener rightListener;
        CharSequence leftText;
        CharSequence rightText;
        boolean cancele = true;
        boolean showLeft = true;
        int gravity = Gravity.CENTER | Gravity.LEFT;

        public Builder setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(CharSequence message) {
            this.message = message;
            return this;
        }

        public Builder setLeftListener(DialogInterface.OnClickListener leftListener) {
            this.leftListener = leftListener;
            return this;
        }

        public Builder setRightListener(DialogInterface.OnClickListener rightListener) {
            this.rightListener = rightListener;
            return this;
        }

        public Builder setLeftText(CharSequence leftText) {
            this.leftText = leftText;
            return this;
        }

        public Builder setRightText(CharSequence rightText) {
            this.rightText = rightText;
            return this;
        }

        public Builder setCancele(boolean cancele) {
            this.cancele = cancele;
            return this;
        }

        public Builder setShowLeft(boolean showLeft) {
            this.showLeft = showLeft;
            return this;
        }

        public Builder setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public DialogView build() {
            return new DialogView() {

                @Override
                public boolean cancele() {
                    return cancele;
                }

                @Override
                public CharSequence getTitle() {
                    return title;
                }

                @Override
                public CharSequence getMessage() {
                    return message;
                }

                @Override
                public DialogInterface.OnClickListener getOnLeftClick() {
                    return leftListener;
                }

                @Override
                public DialogInterface.OnClickListener getOnRightClick() {
                    return rightListener;
                }

                @Override
                public CharSequence getLeftText() {
                    return leftText;
                }

                @Override
                public CharSequence getRightText() {
                    return rightText;
                }

                @Override
                public boolean isLeftShow() {
                    return showLeft;
                }

                @Override
                public int gravity() {
                    return gravity;
                }
            };
        }

    }

    /***
     * 是否可取消
     * @return
     */
    boolean cancele();

    /***
     * 标题
     * @return
     */
    CharSequence getTitle();

    /***
     * 消息内容
     * @return
     */
    CharSequence getMessage();

    /***
     * 左按钮点击事件
     * @return
     */
    DialogInterface.OnClickListener getOnLeftClick();

    /***
     * 右按钮点击事件
     * @return
     */
    DialogInterface.OnClickListener getOnRightClick();

    /***
     * 左按钮文字
     * @return
     */
    CharSequence getLeftText();

    /***
     * 右按钮文字
     * @return
     */
    CharSequence getRightText();

    /***
     * 左按钮是否可见
     * @return
     */
    boolean isLeftShow();

    /*****
     * 内容对齐方式
     * @return
     */
    int gravity();

}
