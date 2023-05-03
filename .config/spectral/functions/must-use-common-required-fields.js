'use strict';

const checkRequiredFields = (targetValues, requiredFields, path ) => {
  const result = [];
  for(const rfItem of Object.values(requiredFields)) {
    if (targetValues.includes(rfItem)) {
      continue;
    }
    else {
      result.push({
        message: `${rfItem} is required field`,
        path: [...path, targetValues, 'required', requiredFields],
      });
      return result
    }
  }
};

module.exports = ( targetValue, { requiredFields }, { path }) => {
  try {
    return checkRequiredFields((targetValue.required), requiredFields, path);
  } catch (ex) {
    return [
      {
        message: ex,
      },
    ];
  }
};