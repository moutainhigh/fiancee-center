package com.njwd.fileexcel.check;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/5 9:53
 */
public interface DataCheckChannel extends DataCheck{

    DataCheck getNext();

    DataCheckChannel setNext(DataCheckChannel dataCheck);

}
