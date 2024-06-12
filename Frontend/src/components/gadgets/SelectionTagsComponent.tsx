import { MultiSelectItemType, MSEnum } from '@/types/';
import React from 'react';
import styled from "styled-components";

function getNameFromItem(item: MultiSelectItemType) {
  let name: string = "";
  
  if(item.type === MSEnum.Instrument){
    name = item.instrumentName ? item.instrumentName : "잘못된 속성명";
  }else if(item.type === MSEnum.Genre){
    name = item.genreName ? item.genreName : "잘못된 속성명";
  }
  
  return name;
}

export interface SelectionTagsComponentProps {
  data: MultiSelectItemType[];
}

const SelectionTagsComponent: React.FC<SelectionTagsComponentProps> = ({ data }) => {
  return (
    data && data.length ? 
    <div>
      {data.map((item: MultiSelectItemType, idx: number) => (
        <S.Tag key={idx}>
          {getNameFromItem(item)}
        </S.Tag>
      ))}
    </div>
    : 
    <></>
  );
};

const S = {
  Tag: styled.div`
    width: 100px;
    background: white;
    margin: 5px;
    padding: 5px;
    border-radius: 5px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  `
}

export default SelectionTagsComponent;