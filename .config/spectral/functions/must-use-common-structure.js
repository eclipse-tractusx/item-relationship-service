'use strict';

module.exports = ( targetValue , { ruleValues }, { path }) => {
  try {
    const result = [];
    function extractProperties(obj){
      let props = []; let i = 0;
      function fillProps(value){props[i] = value; i++;}
      function r(obj1){
        for(let k in obj1) {
          if (typeof obj1[k] == "object") { fillProps(k); r(obj1[k]);}
          else if (typeof k == "string" && k.length > 1 ) { fillProps(k);}
        }}
      r(obj)
      return props;
    }
    if(!(JSON.stringify(extractProperties(targetValue)) === JSON.stringify(extractProperties(ruleValues)))){
      result.push({
        message: `the structure is not the same like the defined rule structure.`,
        path: [...path, targetValue, 'required', ruleValues],
      });
      return result
    }
  } catch (ex) {
    return [
      {
        message: ex,
      },
    ];
  }
};