'use strict';

module.exports = ({security}) => {
  if (Object.keys(security).length === 0){
    return [{message: 'Security Requirement objects with no properties define no security'}];
  }
};