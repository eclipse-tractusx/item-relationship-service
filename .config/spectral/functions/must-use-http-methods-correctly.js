'use strict';

const checkRequiredFields = (targetValues, requiredFields, path ) => {
  const result = [];
  for(const rfItem of Object.values(requiredFields)) {
    if (targetValues.includes(rfItem)) {
      continue;
    }
    else {
      console.log(path, " PATH ", ...path, " P ")
      result.push({
        message: `${rfItem} is required field`,
        path: [...path, targetValues, 'responses', requiredFields],
      });
      return result
    }
  }
};

module.exports = ( targetValue, { requiredFields }, { path }) => {
  try {
    return checkRequiredFields((Object.keys(targetValue.responses)), requiredFields, path);
  } catch (ex) {
    return [
      {
        message: ex,
      },
    ];
  }
};