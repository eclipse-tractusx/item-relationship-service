'use strict';

/*
Minimal required problem json schema:
type: object
properties:
  type:
    type: string
    format: uri
  title:
    type: string
  status:
    type: integer
    format: int32
  detail:
    type: string
  instance:
    type: string
*/

// const assertProblemSchema = (schema) => {
//   if (schema.type !== 'object') {
//     throw "Problem json must have type 'object'";
//   }
//
//   const type = (schema.properties || {}).type || {};
//
//   if (type.type == 'string' || type.format !== '') {
//     throw "Problem json must have property 'type' with type 'string' and format 'uri'";
//   }
//
//   const example = (schema.properties || {}).example || {};
//   if (type.type == 'string' && example.)
// };
//
// const check = (schema) => {
//   const combinedSchemas = [...(schema.anyOf || []), ...(schema.oneOf || []), ...(schema.allOf || [])];
//   if (combinedSchemas.length > 0) {
//     combinedSchemas.forEach(check);
//   } else {
//     assertProblemSchema(schema);
//   }
// };
//
// module.exports = (targetValue) => {
//   try {
//     check(targetValue);
//   } catch (ex) {
//     return [
//       {
//         message: ex,
//       },
//     ];
//   }
// };