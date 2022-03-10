# edc-recursive-job

The logic to orchestate multiple transfers (to build a PRS parts tree out of recursive calls to PRS API endpoints in multiple dataspace partitions) is factored out into a generic framework to manage jobs (in this module), that allows plugging in a custom handler to provide the logic, in our case to parse the output of one PRS API call to determine the next endpoints to call.

We are in discussions with the EDC team to determine if this module could be integrated upstream.
